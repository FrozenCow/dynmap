package org.dynmap.authentication;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements JSONAware {
    public String OpenID;
    public String PlayerName;
    
    /* Player is filled when the player is online */
    Map<String, Boolean> permissions = new HashMap<String, Boolean>();
    
    public User(String playerName) {
        this.PlayerName = playerName;
    }
    
    public User(String playerName, String openId) {
        this.PlayerName = playerName;
        this.OpenID = openId;
    }

    // Helper method, so 'Player' can be removed in the future (once we can get permissions from OfflinePlayer).
    public boolean hasPermission(String permission) {
        Boolean b = permissions.get(permission);
        return b != null ? b.booleanValue() : false;
    }

    // This function is pretty awesome. it copies the permissions-table from the player and puts it in the local
    // permissions-table. This way we still know what permissions the player has after the player logged out.
    // TODO: Have a proper way to access offline player permissions.
    public void updatePermissions(Player player) {
        permissions.clear();
        for(PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions())
            permissions.put(attachmentInfo.getPermission(), attachmentInfo.getValue());
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
