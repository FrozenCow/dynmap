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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.dynmap.Component;
import org.dynmap.ConfigurationNode;
import org.dynmap.DynmapPlugin;
import org.dynmap.servlet.JSONServlet;
import org.json.simple.JSONObject;
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
    
    public final OpenIDProviderStore openIDProviderStore;
    public final UserStore userStore;
    
    public AuthenticationComponent(final DynmapPlugin plugin, ConfigurationNode configuration) throws ConsumerException {
        super(plugin, configuration);

        openIDProviderStore = new OpenIDProviderStore(plugin);
        userStore = new UserStore(plugin);
        setupUserStore();
        
        consumerManager = new ConsumerManager();
        setupAuthenticationServlets();
    }
    
    void setupUserStore() {
        // Remember the currently online 'Player' objects in 'User'.
        Player[] players = plugin.getServer().getOnlinePlayers();
        for(int i = 0; i < players.length; i++) {
            User u = userStore.getUserByPlayerName(players[i].getName());
            if (u != null) {
                u.Player = players[i];
            }
        }
        // When new players join, remember the 'Player' object in 'User'.
        plugin.registerEvent(Event.Type.PLAYER_LOGIN, new PlayerListener() {
            @Override
            public void onPlayerLogin(PlayerLoginEvent event) {
                super.onPlayerLogin(event);
                User u = userStore.getUserByPlayerName(event.getPlayer().getName());
                if (u != null) {
                    u.Player = event.getPlayer();
                }
            }
        });
    }
    
    void setupAuthenticationServlets() {
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
                boolean json = "1".equals(req.getParameter("json"));
                User user = userStore.getUserByPlayerName(playername);
                if (user == null) {
                    final String errorMessage = "You have not yet associated your game character with this account. Use the in-game command '/dynmap setwebuser <provider>' and try to login again here.";
                    if (json) {
                        JSONServlet.respond(resp, new JSONObject() {{
                            this.put("error", errorMessage);
                        }});
                    } else {
                        resp.setContentType("text/plain");
                        ServletOutputStream s = resp.getOutputStream();
                        s.print(errorMessage);
                        s.close();
                    }
                    return;
                }
                try {
                    List discoveries = consumerManager.discover(user.OpenID);
                    DiscoveryInformation discovered = consumerManager.associate(discoveries);
                    req.getSession().setAttribute("openid-disc", discovered);
                    req.getSession().setAttribute("authenticatinguser", user);
                    final AuthRequest authReq = consumerManager.authenticate(discovered, verifyURL + "?originalurl=" + URLEncoder.encode(originalURL, "UTF-8"));
                    
                    // For now, we don't need the Email-extension
                    /*FetchRequest fetch = FetchRequest.createFetchRequest();
                    fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
                    authReq.addExtension(fetch);*/
                    if (json) {
                        // JSON mode so that javascript can handle this seamlessly.
                        if (!discovered.isVersion2()) {
                            JSONServlet.respond(resp, new JSONObject() {{
                                this.put("url", authReq.getDestinationUrl(true));
                            }});
                        } else {
                            final Map<?, ?> m = (Map<?, ?>)authReq.getParameterMap();
                            JSONServlet.respond(resp, new JSONObject() {{
                                this.put("url", authReq.getDestinationUrl(false));
                                this.put("parameters", new JSONObject() {{
                                    for(Map.Entry<?, ?> entry : m.entrySet()) {
                                        this.put(entry.getKey(), entry.getValue());
                                    }
                                }});
                            }});
                        }
                    } else {
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
