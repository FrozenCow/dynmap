package org.dynmap.authentication;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dynmap.Component;
import org.dynmap.ConfigurationNode;
import org.dynmap.DynmapPlugin;
import org.dynmap.WebMessageEvent;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ax.FetchRequest;

public class AuthenticationComponent extends Component {
    String authenticationResponseURL = "http://kabel.vanderlinden.cx:22000/up/authenticate";
    final ConsumerManager consumerManager;
    HttpServlet authenticationRequestServlet;
    HttpServlet authenticationResponseServlet;
    
    public AuthenticationComponent(DynmapPlugin plugin, ConfigurationNode configuration) throws ConsumerException {
        super(plugin, configuration);
        consumerManager = new ConsumerManager();
        
        authenticationRequestServlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                // User supplied string
                String uss = req.getParameter("uss");
                try {
                    List discoveries = consumerManager.discover(uss);
                    DiscoveryInformation discovered = consumerManager.associate(discoveries);
                    req.getSession().setAttribute("openid-disc", discovered);
                    AuthRequest authReq = consumerManager.authenticate(discovered, authenticationResponseURL);
                    
                    // Use Email-extension
                    FetchRequest fetch = FetchRequest.createFetchRequest();
                    fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
                    authReq.addExtension(fetch);
                    
                    if (! discovered.isVersion2() ) {
                        resp.sendRedirect(authReq.getDestinationUrl(true));
                    }
                    else {
                        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("formredirection.jsp");
                        req.setAttribute("parameterMap", authReq.getParameterMap());
                        req.setAttribute("destinationUrl", authReq.getDestinationUrl(false));
                        dispatcher.forward(req, resp);
                    }
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
