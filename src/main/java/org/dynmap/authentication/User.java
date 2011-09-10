package org.dynmap.authentication;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class User implements JSONAware {
    public String OpenID;
    public String PlayerName;
    
    public User(String playerName) {
        this.PlayerName = playerName;
    }
    
    public User(String playerName, String openId) {
        this.PlayerName = playerName;
        this.OpenID = openId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return ((User)obj).PlayerName.equals(PlayerName);
        }
        return super.equals(obj);
    }

    @Override
    public String toJSONString() {
        return "{\"openid\":\"" + JSONObject.escape(OpenID) + "\",\"playername\":\"" + PlayerName + "\"}"; 
    }
}
