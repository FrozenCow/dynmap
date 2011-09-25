package org.dynmap;

import static org.dynmap.JSONUtils.s;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.json.simple.JSONObject;

public class SimpleWebChatComponent extends Component {

    public SimpleWebChatComponent(final DynmapPlugin plugin, final ConfigurationNode configuration) {
        super(plugin, configuration);
        plugin.events.addListener("webmessage_chat", new Event.Listener<WebMessageEvent>() {
            @Override
            public void triggered(WebMessageEvent t) {
                if (t.user == null && !configuration.getBoolean("allowanonymouschat", false)) {
                    return;
                }
                String userName = t.user != null
                        ? t.user.PlayerName
                        : (t.message.containsKey("name")
                            ? String.valueOf(t.message.get("name"))
                            : "Anonymous");
                String message = String.valueOf(t.message.get("message"));
                
                DynmapWebChatEvent evt = new DynmapWebChatEvent("web", userName, message);
                plugin.getServer().getPluginManager().callEvent(evt);
                if(!evt.isCancelled()) {
                    plugin.getServer().broadcastMessage(unescapeString(plugin.configuration.getString("webprefix", "\u00A72[WEB] ")) + userName + ": " + unescapeString(plugin.configuration.getString("websuffix", "\u00A7f")) + message);
                    
                    plugin.mapManager.pushUpdate(new Client.ChatMessage("web", "", userName, message, userName));
                }
            }
        });
        
        plugin.events.addListener("buildclientconfiguration", new Event.Listener<JSONObject>() {
            @Override
            public void triggered(JSONObject t) {
                s(t, "allowchat", configuration.getBoolean("allowchat", false));
            }
        });
        
        if (configuration.getBoolean("allowchat", false)) {
            PlayerChatListener playerListener = new PlayerChatListener();
            plugin.registerEvent(org.bukkit.event.Event.Type.PLAYER_CHAT, playerListener);
            plugin.registerEvent(org.bukkit.event.Event.Type.PLAYER_LOGIN, playerListener);
            plugin.registerEvent(org.bukkit.event.Event.Type.PLAYER_JOIN, playerListener);
            plugin.registerEvent(org.bukkit.event.Event.Type.PLAYER_QUIT, playerListener);
        }
    }
    
    protected class PlayerChatListener extends PlayerListener {
        @Override
        public void onPlayerChat(PlayerChatEvent event) {
            if(event.isCancelled()) return;
            if(plugin.mapManager != null)
                plugin.mapManager.pushUpdate(new Client.ChatMessage("player", "", event.getPlayer().getDisplayName(), event.getMessage(), event.getPlayer().getName()));
        }

        @Override
        public void onPlayerJoin(PlayerJoinEvent event) {
            if(plugin.mapManager != null)
                plugin.mapManager.pushUpdate(new Client.PlayerJoinMessage(event.getPlayer().getDisplayName(), event.getPlayer().getName()));
        }

        @Override
        public void onPlayerQuit(PlayerQuitEvent event) {
            if(plugin.mapManager != null)
                plugin.mapManager.pushUpdate(new Client.PlayerQuitMessage(event.getPlayer().getDisplayName(), event.getPlayer().getName()));
        }
    }

}
