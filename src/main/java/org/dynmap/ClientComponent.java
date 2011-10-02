package org.dynmap;

import static org.dynmap.JSONUtils.a;
import static org.dynmap.JSONUtils.s;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
public class ClientComponent extends Component {

    public ClientComponent(final DynmapPlugin plugin, final ConfigurationNode configuration) {
        super(plugin, configuration);
        plugin.events.addListener("buildclientconfiguration", new Event.Listener<BuildJsonEvent>() {
            @Override
            public void triggered(BuildJsonEvent event) {
                buildClientConfiguration(event);
            }
        });
    }
    
    protected void buildClientConfiguration(BuildJsonEvent event) {
        JSONObject o = createClientConfiguration();
        a(event.getJson(), "components", o);
    }
    
    protected JSONObject createClientConfiguration() {
        JSONObject o = convertMap(configuration);
        o.remove("class");
        return o;
    }
    
    protected static final JSONObject convertMap(Map<String, ?> m) {
        JSONObject o = new JSONObject();
        for(Map.Entry<String, ?> entry : m.entrySet()) {
            s(o, entry.getKey(), convert(entry.getValue()));
        }
        return o;
    }
    
    @SuppressWarnings("unchecked")
    protected static final JSONArray convertList(List<?> l) {
        JSONArray o = new JSONArray();
        for(Object entry : l) {
            o.add(convert(entry));
        }
        return o;
    }
    
    @SuppressWarnings("unchecked")
    protected static final Object convert(Object o) {
        if (o instanceof Map<?, ?>) {
            return convertMap((Map<String, ?>)o);
        } else if (o instanceof List<?>) {
            return convertList((List<?>)o);
        }
        return o;
    }

}
