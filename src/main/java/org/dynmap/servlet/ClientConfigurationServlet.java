package org.dynmap.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dynmap.BuildJsonEvent;
import org.dynmap.DynmapPlugin;
import org.dynmap.DynmapWorld;
import org.dynmap.Event;
import org.json.simple.JSONObject;
import static org.dynmap.Utils.*;
import org.dynmap.authentication.User;

public class ClientConfigurationServlet extends HttpServlet {
    private static final long serialVersionUID = 9106801553080522469L;
    private DynmapPlugin plugin;
    private byte[] cachedConfiguration = null;
    public ClientConfigurationServlet(DynmapPlugin plugin) {
        this.plugin = plugin;
        plugin.events.addListener("worldactivated", new Event.Listener<DynmapWorld>() {
            @Override
            public void triggered(DynmapWorld t) {
                cachedConfiguration = null;
            }
        });
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = as(User.class, req.getSession().getAttribute("user"));
        byte[] outputBytes = cachedConfiguration;
        if (user == null || outputBytes == null) {
            BuildJsonEvent buildEvent = new BuildJsonEvent(user, new JSONObject());
            plugin.events.<BuildJsonEvent>trigger("buildclientconfiguration", buildEvent);
            
            String s = buildEvent.getJson().toJSONString();
            
            outputBytes = s.getBytes("UTF-8");
        }
        if (user == null && outputBytes == null) {
            cachedConfiguration = outputBytes;
        }
        String dateStr = new Date().toString();
        res.addHeader("Date", dateStr);
        res.setContentType("text/plain; charset=utf-8");
        res.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
        res.addHeader("Last-modified", dateStr);
        res.setContentLength(outputBytes.length);
        res.getOutputStream().write(outputBytes);
    }
}
