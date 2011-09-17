package org.dynmap.authentication;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dynmap.Component;
import org.dynmap.ConfigurationNode;
import org.dynmap.DynmapPlugin;
import org.json.simple.JSONValue;
import org.openid4java.OpenIDException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;

public class AuthenticationComponent extends Component {
    final ConsumerManager consumerManager;
    HttpServlet authenticationRequestServlet;
    HttpServlet authenticationVerifyServlet;
    HttpServlet authenticationTestServlet;
    
    public AuthenticationComponent(final DynmapPlugin plugin, ConfigurationNode configuration) throws ConsumerException {
        super(plugin, configuration);
        consumerManager = new ConsumerManager();
        consumerManager.setAssociations(new InMemoryConsumerAssociationStore());
        consumerManager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        consumerManager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
        
        authenticationTestServlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                Object o = req.getSession().getAttribute("user");
                Object result = o != null ? o : false;
                ServletOutputStream s = resp.getOutputStream();
                s.print(JSONValue.toJSONString(result));
                s.close();
            }
        };
        
        authenticationRequestServlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                String verifyURL = req.getParameter("verifyurl");
                String originalURL = req.getParameter("originalurl");
                String playername = req.getParameter("playername");
                User user = plugin.userStore.getUserByPlayerName(playername);
                if (user == null) {
                    resp.setContentType("text/plain");
                    ServletOutputStream s = resp.getOutputStream();
                    s.print("You have not yet associated your game character with this account. Use the in-game command '/dynmap setwebuser <username> <provider>' and try to login again here.");
                    s.close();
                    return;
                }
                try {
                    List discoveries = consumerManager.discover(user.OpenID);
                    DiscoveryInformation discovered = consumerManager.associate(discoveries);
                    req.getSession().setAttribute("openid-disc", discovered);
                    req.getSession().setAttribute("authenticatinguser", user);
                    AuthRequest authReq = consumerManager.authenticate(discovered, verifyURL + "?originalurl=" + URLEncoder.encode(originalURL, "UTF-8"));
                    
                    // For now, we don't need the Email-extension
                    /*FetchRequest fetch = FetchRequest.createFetchRequest();
                    fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
                    authReq.addExtension(fetch);*/
                    
                    if (! discovered.isVersion2() ) {
                        resp.sendRedirect(authReq.getDestinationUrl(true));
                    }
                    else {
                        Map<?, ?> m = (Map<?, ?>)authReq.getParameterMap();
                        StringBuffer body = new StringBuffer();
                        body.append("<html><body onload='document.forms[0].submit()'><form name='form' action='");
                        body.append(authReq.getDestinationUrl(false));
                        body.append("'>");
                        for(Map.Entry<?, ?> entry : m.entrySet()) {
                            body.append("<input type='hidden' name='");
                            body.append(entry.getKey());
                            body.append("' value='");
                            body.append(entry.getValue());
                            body.append("'/>");
                        }
                        body.append("</form></body></html>");
                        
                        resp.setContentType("text/html");
                        ServletOutputStream s = resp.getOutputStream();
                        s.print(body.toString());
                        s.close();
                     }
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        
        authenticationVerifyServlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                try {
                    ParameterList response = new ParameterList(req.getParameterMap());
                    
                    DiscoveryInformation discovered = (DiscoveryInformation)req.getSession().getAttribute("openid-disc");
                    User user = (User)req.getSession().getAttribute("authenticatinguser");
                    req.getSession().removeAttribute("openid-disc");
                    req.getSession().removeAttribute("authenticatinguser");
                    
                    
                    // extract the receiving URL from the HTTP request
                    StringBuffer receivingURL = req.getRequestURL();
                    String queryString = req.getQueryString();
                    if (queryString != null && queryString.length() > 0)
                        receivingURL.append("?").append(req.getQueryString());

                    // verify the response; ConsumerManager needs to be the same
                    // (static) instance used to place the authentication request
                    VerificationResult verification = consumerManager.verify(receivingURL.toString(), response, discovered);
                    
                    // examine the verification result and extract the verified identifier
                    Identifier verified = verification.getVerifiedId();
                    
                    if (verified != null) {
                        String identifier = verified.getIdentifier();

                        // Strange situation
                        if (user == null) {
                            resp.setContentType("text/plain");
                            ServletOutputStream s = resp.getOutputStream();
                            s.print("Login failed. You have not yet associated your game character with this account. Go in-game and type '/dynmap setwebuser " + identifier + "' and log in again.");
                            s.close();
                            return;
                        } else {
                            req.getSession().setAttribute("user", user);
                            String originalurl = req.getParameter("originalurl");
                            if (originalurl != null)
                                resp.sendRedirect(originalurl);
                            else
                                resp.sendRedirect("/");
                        }
                    }
                } catch(OpenIDException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        
        plugin.addServlet("/up/authentication/test", authenticationTestServlet);
        plugin.addServlet("/up/authentication/request", authenticationRequestServlet);
        plugin.addServlet("/up/authentication/verify", authenticationVerifyServlet);
        plugin.addServlet("/up/authentication/logout", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                req.getSession().removeAttribute("user");
            }
        });
    }
}
