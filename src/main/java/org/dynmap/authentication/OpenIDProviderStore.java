package org.dynmap.authentication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dynmap.DynmapPlugin;

public class OpenIDProviderStore {
    DynmapPlugin plugin;
    File file;
    Map<String, OpenIDProvider> openidProviders = new HashMap<String, OpenIDProvider>();
    
    public OpenIDProviderStore(DynmapPlugin plugin) {
        this.plugin = plugin;
        file = plugin.getFile("openidproviders.txt");
        load();
    }
    
    public synchronized void load() {
        try {
            openidProviders.clear();
            
            if (!file.exists() && !plugin.createDefaultFileFromResource("/openidproviders.txt", file)) {
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] parts = line.split(" |\t");
                openidProviders.put(parts[0].toLowerCase(), new OpenIDProvider(parts[0], parts[1]));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public OpenIDProvider Get(String name) {
        return openidProviders.get(name.toLowerCase());
    }
    
    public Set<String> GetProviderNames() {
        return openidProviders.keySet();
    }
    
    public static class OpenIDProvider {
        public String name;
        public String url;
        
        public OpenIDProvider(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
