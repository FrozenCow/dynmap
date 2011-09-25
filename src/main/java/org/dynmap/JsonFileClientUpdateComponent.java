package org.dynmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.World;
import org.dynmap.authentication.User;
import org.dynmap.web.Json;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static org.dynmap.JSONUtils.*;
import java.nio.charset.Charset;

public class JsonFileClientUpdateComponent extends ClientUpdateComponent {
    protected long jsonInterval;
    protected long currentTimestamp = 0;
    protected long lastTimestamp = 0;
    protected JSONParser parser = new JSONParser();

    private HashMap<String,String> useralias = new HashMap<String,String>();
    private int aliasindex = 1;
    
    private Charset cs_utf8 = Charset.forName("UTF-8");
    public JsonFileClientUpdateComponent(final DynmapPlugin plugin, final ConfigurationNode configuration) {
        super(plugin, configuration);
        jsonInterval = (long)(configuration.getFloat("writeinterval", 1) * 1000);
        MapManager.scheduleDelayedJob(new Runnable() {
            @Override
            public void run() {
                currentTimestamp = System.currentTimeMillis();
                writeUpdates();
                handleWebMessages();
                lastTimestamp = currentTimestamp;
                MapManager.scheduleDelayedJob(this, jsonInterval);
            }}, jsonInterval);
        
        plugin.events.addListener("buildclientconfiguration", new Event.Listener<JSONObject>() {
            @Override
            public void triggered(JSONObject t) {
                s(t, "jsonfile", true);
            }
        });
        plugin.events.addListener("initialized", new Event.Listener<Object>() {
            @Override
            public void triggered(Object t) {
                writeConfiguration();
            }
        });
        plugin.events.addListener("worldactivated", new Event.Listener<DynmapWorld>() {
            @Override
            public void triggered(DynmapWorld t) {
                writeConfiguration();
            }
        });
    }
    
    protected File getStandaloneFile(String filename) {
        File webpath = new File(plugin.configuration.getString("webpath", "web"), "standalone/" + filename);
        if (webpath.isAbsolute())
            return webpath;
        else
            return new File(plugin.getDataFolder(), webpath.toString());
    }
    
    private static final int RETRY_LIMIT = 5;
    protected void writeConfiguration() {
        File outputFile;
        File outputTempFile;
        JSONObject clientConfiguration = new JSONObject();
        plugin.events.trigger("buildclientconfiguration", clientConfiguration);
        outputFile = getStandaloneFile("dynmap_config.json");
        outputTempFile = getStandaloneFile("dynmap_config.json.new");
        
        int retrycnt = 0;
        boolean done = false;
        while(!done) {
            try {
                FileOutputStream fos = new FileOutputStream(outputTempFile);
                fos.write(clientConfiguration.toJSONString().getBytes("UTF-8"));
                fos.close();
                outputFile.delete();
                outputTempFile.renameTo(outputFile);
                done = true;
            } catch (IOException ioe) {
                if(retrycnt < RETRY_LIMIT) {
                    try { Thread.sleep(20 * (1 << retrycnt)); } catch (InterruptedException ix) {}
                    retrycnt++;
                }
                else {
                    Log.severe("Exception while writing JSON-configuration-file.", ioe);
                    done = true;
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void writeUpdates() {
        File outputFile;
        File outputTempFile;
        if(plugin.mapManager == null) return;
        //Handles Updates
        for (DynmapWorld dynmapWorld : plugin.mapManager.getWorlds()) {
            World world = dynmapWorld.world;

            JSONObject update = new JSONObject();
            update.put("timestamp", currentTimestamp);
            ClientUpdateEvent clientUpdate = new ClientUpdateEvent(currentTimestamp - 30000, dynmapWorld, update);
            plugin.events.trigger("buildclientupdate", clientUpdate);

            outputFile = getStandaloneFile("dynmap_" + world.getName() + ".json");
            outputTempFile = getStandaloneFile("dynmap_" + world.getName() + ".json.new");
            int retrycnt = 0;
            boolean done = false;
            while(!done) {
                try {
                    FileOutputStream fos = new FileOutputStream(outputTempFile);
                    fos.write(Json.stringifyJson(update).getBytes("UTF-8"));
                    fos.close();
                    outputFile.delete();
                    outputTempFile.renameTo(outputFile);
                    done = true;
                } catch (IOException ioe) {
                    if(retrycnt < RETRY_LIMIT) {
                        try { Thread.sleep(20 * (1 << retrycnt)); } catch (InterruptedException ix) {}
                        retrycnt++;
                    }
                    else {
                        Log.severe("Exception while writing JSON-file.", ioe);
                        done = true;
                    }
                }
            }
            plugin.events.<ClientUpdateEvent>trigger("clientupdatewritten", clientUpdate);
        }
        
        plugin.events.<Object>trigger("clientupdateswritten", null);
    }

    protected void handleWebMessages() {
        File webmessagesFile = getStandaloneFile("dynmap_webmessages.json");
        if (webmessagesFile.exists() && lastTimestamp != 0) {
            JSONArray jsonMsgs = null;
            try {
                Reader inputFileReader = new InputStreamReader(new FileInputStream(webmessagesFile), cs_utf8);
                jsonMsgs = (JSONArray) parser.parse(inputFileReader);
                inputFileReader.close();
            } catch (IOException ex) {
                Log.severe("Exception while reading JSON-file.", ex);
            } catch (ParseException ex) {
                Log.severe("Exception while parsing JSON-file.", ex);
            }

            if (jsonMsgs != null) {
                Iterator<?> iter = jsonMsgs.iterator();
                while (iter.hasNext()) {
                    JSONObject o = (JSONObject) iter.next();
                    String ts = String.valueOf(o.get("timestamp"));
                    if(ts.equals("null")) ts = "0";
                    if (Long.parseLong(ts) >= (lastTimestamp)) {
                        String type = String.valueOf(o.get("type"));
                        JSONObject message = (JSONObject)o.get("message");
                        String playerName = String.valueOf(o.get("playerName"));
                        User user = playerName != null ? new User(playerName) : null;
                        WebMessageEvent evt = new WebMessageEvent(type, message, user);
                        plugin.events.trigger("webmessage", evt);
                        plugin.events.trigger("webmessage_" + type, evt);
                    }
                }
            }
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}
