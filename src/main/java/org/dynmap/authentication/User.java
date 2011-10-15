package org.dynmap.authentication;

import org.bukkit.entity.Player;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class User implements JSONAware {
    public String OpenID;
    public String PlayerName;
    
    /* Player is filled when the player is online */
    public Player Player;
    
    public User(String playerName) {
        this.PlayerName = playerName;
    }
    
    public User(String playerName, String openId) {
        this.PlayerName = playerName;
        this.OpenID = openId;
    }

    // Helper method, so 'Player' can be removed in the future (once we can get permissions from OfflinePlayer).
    public boolean hasPermission(String permission) {
        return Player != null
            ? Player.hasPermission(permission)
            : false /* TODO: What should be the default permission? */;
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
