package org.dynmap.authentication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.dynmap.DynmapPlugin;
import org.dynmap.Log;
import org.dynmap.utils.NewMapChunkCache;
import org.yaml.snakeyaml.reader.StreamReader;

public class UserStore {
    File file;
    List<User> users = new LinkedList<User>(); 
    Map<String, User> openidUserMap = new HashMap<String, User>();
    Map<String, User> playerUserMap = new HashMap<String, User>();
    
    public UserStore(DynmapPlugin plugin) {
        file = plugin.getFile("users.txt");
        loadUsers();
    }
    
    public synchronized void loadUsers() {
        try {
            openidUserMap.clear();
            playerUserMap.clear();
            users.clear();
            
            if (!file.exists()) {
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                
                int separator = line.indexOf(' ');
                User u = null;
                if (separator < 0) {
                    u = new User(line);
                } else {
                    u = new User(line.substring(0, separator), line.substring(separator+1));
                }
                if (u.OpenID != null) {
                    openidUserMap.put(u.OpenID, u);
                }
                if (u.PlayerName != null) {
                    playerUserMap.put(u.PlayerName, u);
                }
                users.add(u);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void saveUsers() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(User user : users) {
                writer.write(user.PlayerName);
                if (user.OpenID != null) {
                    writer.write(' ');
                    writer.write(user.OpenID);
                }
                writer.newLine();
            }
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized User getUserByOpenID(String openId) {
        return openidUserMap.get(openId);
    }
    
    public synchronized User getUserByPlayerName(String playerName) {
        return playerUserMap.get(playerName);
    }
    
    public synchronized boolean deleteUser(String playerName) {
        User user = getUserByPlayerName(playerName);
        if (user == null) {
            return false;
        }
        if (!users.remove(user)) {
            Log.warning("Not in userlist?");
        }
        if (user.OpenID != null && openidUserMap.remove(user.OpenID) != user) {
            Log.warning("Not correct openid removed?");
        }
        if (user.PlayerName != null && playerUserMap.remove(user.PlayerName) != user) {
            Log.warning("Not correct player removed?");
        }
        saveUsers();
        return true;
    }

    public synchronized  User setOpenID(Player player, String openId) {
        User user = setOpenID(player.getName(), openId);
        user.Player = player;
        return user;
    }

    public synchronized User setOpenID(String playerName, String openId) {
        User user = getUserByPlayerName(playerName);
        if (user == null) {
            user = new User(playerName);
            users.add(user);
            playerUserMap.put(playerName, user);
        } else  if (user.OpenID != null) {
            openidUserMap.remove(user.OpenID);
        }
        user.OpenID = openId;
        if (openId != null) {
            openidUserMap.put(openId, user);
        }
        saveUsers();
        return user;
    }
}
