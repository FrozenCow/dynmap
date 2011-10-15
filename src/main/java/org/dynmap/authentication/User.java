package org.dynmap.authentication;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;
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
        List<PermissionAttachment> attachments = null;
        try {
            Field permField = Class.forName("org.bukkit.craftbukkit.entity.CraftHumanEntity").getDeclaredField("perm");
            permField.setAccessible(true);
            PermissibleBase pb = (PermissibleBase)permField.get(player);
            Field attachmentsField = PermissibleBase.class.getDeclaredField("attachments");
            attachmentsField.setAccessible(true);
            attachments = (List<PermissionAttachment>)attachmentsField.get(pb);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (attachments != null) {
            for(int i = 0; i < attachments.size(); i++) {
                permissions.putAll(attachments.get(i).getPermissions());
            }
        }
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
