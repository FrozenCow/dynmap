package org.dynmap;

import org.dynmap.Event.Listener;
import org.dynmap.servlet.ClientUpdateServlet;
import org.dynmap.web.handlers.ClientUpdateHandler;
import org.dynmap.web.handlers.SendMessageHandler;
import org.json.simple.JSONObject;
import static org.dynmap.JSONUtils.*;

public class InternalClientUpdateComponent extends ClientUpdateComponent {

    public InternalClientUpdateComponent(DynmapPlugin plugin, final ConfigurationNode configuration) {
        super(plugin, configuration);
        plugin.addServlet("/up/world/*", new ClientUpdateServlet(plugin));
    }
}
