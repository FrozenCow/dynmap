package org.dynmap;

import org.dynmap.authentication.User;
import org.json.simple.JSONObject;

public class BuildJsonEvent {
    private User user;
    private JSONObject json;
    public BuildJsonEvent(User user, JSONObject json) {
        this.user = user;
        this.json = json;
    }
    public User getUser() {
        return user;
    }
    public JSONObject getJson() {
        return json;
    }
}
