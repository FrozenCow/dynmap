package org.dynmap.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dynmap.DynmapPlugin;
import org.dynmap.Log;
import org.dynmap.WebMessageEvent;
import org.dynmap.web.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SendWebMessageServlet extends HttpServlet {
    private final JSONParser parser = new JSONParser();
    private final DynmapPlugin plugin;
    
    public SendWebMessageServlet(DynmapPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        JSONObject o;
        try {
            o = (JSONObject)parser.parse(reader);
        } catch (ParseException e) {
            resp.sendError(HttpStatus.BadRequest.getCode());
            return;
        }
        Object messageTypeObject = o.get("type");
        Object messageObject = o.get("message");
        if (!(messageTypeObject instanceof String) || !(messageObject instanceof JSONObject)) {
            resp.sendError(HttpStatus.BadRequest.getCode());
            return;
        }
        String messageType = (String)messageTypeObject;
        JSONObject message = (JSONObject)messageObject;

        Log.info("WebMessage(" + messageType + "):" + message.toJSONString());
        plugin.events.trigger("webmessage", new WebMessageEvent(messageType, message));
        plugin.events.trigger("webmessage_"+messageType, new WebMessageEvent(messageType, message));
    }
}
