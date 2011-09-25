package org.dynmap;

import org.dynmap.authentication.User;
import org.json.simple.JSONObject;

public class WebMessageEvent {
    public String type;
    public JSONObject message;
    public User user;
    public WebMessageEvent(String type, JSONObject message) {
        this.type = type;
        this.message = message;
        this.user = null;
    }
    
    public WebMessageEvent(String type, JSONObject message, User user) {
        this.type = type;
        this.message = message;
        this.user = user;
    }
}
