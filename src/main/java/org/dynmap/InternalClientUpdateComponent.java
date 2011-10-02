package org.dynmap;

import org.dynmap.servlet.ClientUpdateServlet;
import org.dynmap.servlet.SendWebMessageServlet;

public class InternalClientUpdateComponent extends ClientUpdateComponent {

    public InternalClientUpdateComponent(final DynmapPlugin plugin, final ConfigurationNode configuration) {
        super(plugin, configuration);
        
        plugin.addServlet("/up/world/*", new ClientUpdateServlet(plugin));
        plugin.addServlet("/up/sendwebmessage", new SendWebMessageServlet(plugin));
    }
}
