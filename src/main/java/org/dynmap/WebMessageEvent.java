package org.dynmap;

import org.json.simple.JSONObject;

public class WebMessageEvent {
    public String type;
    public JSONObject message;
    public WebMessageEvent(String type, JSONObject message) {
        this.type = type;
        this.message = message;
    }
}
