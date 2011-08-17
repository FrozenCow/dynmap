package org.dynmap;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.dynmap.Event.Listener;
import org.dynmap.web.HttpField;
import org.dynmap.web.HttpHandler;
import org.dynmap.web.HttpMethod;
import org.dynmap.web.HttpRequest;
import org.dynmap.web.HttpResponse;
import org.dynmap.web.HttpStatus;
import org.dynmap.web.handlers.ClientUpdateHandler;
import org.dynmap.web.handlers.SendMessageHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static org.dynmap.JSONUtils.*;

public class InternalClientUpdateComponent extends ClientUpdateComponent {

    public InternalClientUpdateComponent(final DynmapPlugin plugin, final ConfigurationNode configuration) {
        super(plugin, configuration);
        final Boolean allowwebchat = configuration.getBoolean("allowwebchat", false);
        final Boolean hidewebchatip = configuration.getBoolean("hidewebchatip", false);
        final Boolean trust_client_name = configuration.getBoolean("trustclientname", false);
        final float webchatInterval = configuration.getFloat("webchat-interval", 1);
        final String spammessage = plugin.configuration.getString("spammessage", "You may only chat once every %interval% seconds.");

        plugin.events.addListener("buildclientconfiguration", new Event.Listener<JSONObject>() {
            @Override
            public void triggered(JSONObject t) {
                s(t, "allowwebchat", allowwebchat);
                s(t, "webchat-interval", webchatInterval);
            }
        });
        
        // TODO: Uncomment!
        //plugin.webServer.handlers.put("/up/", new ClientUpdateHandler(plugin));
        
        // TODO: Uncomment!
        /*plugin.webServer.handlers.put("/up/sendwebmessage", new HttpHandler() {
            private final JSONParser parser = new JSONParser();
            private Charset cs_utf8 = Charset.forName("UTF-8");
            @Override
            public void handle(String path, HttpRequest request, HttpResponse response) throws Exception {
                if (!request.method.equals(HttpMethod.Post)) {
                    response.status = HttpStatus.MethodNotAllowed;
                    response.fields.put(HttpField.Accept, HttpMethod.Post);
                    return;
                }

                InputStreamReader reader = new InputStreamReader(request.body, cs_utf8);
                JSONObject o = (JSONObject)parser.parse(reader);
                Object messageTypeObject = o.get("type");
                Object messageObject = o.get("message");
                if (!(messageTypeObject instanceof String) || !(messageObject instanceof JSONObject)) {
                    response.status = HttpStatus.BadRequest;
                    return;
                }
                String messageType = (String)messageTypeObject;
                JSONObject message = (JSONObject)messageObject;

                Log.info("WebMessage(" + messageType + "):" + message.toJSONString());
                plugin.events.trigger("webmessage", new WebMessageEvent(messageType, message));
                
                response.fields.put(HttpField.ContentLength, "0");
                response.status = HttpStatus.OK;
                response.getBody();
            }
        });
        
        if (allowwebchat) {
            SendMessageHandler messageHandler = new SendMessageHandler() {{
                maximumMessageInterval = (int)(webchatInterval * 1000);
                spamMessage = "\""+spammessage+"\"";
                hideip = hidewebchatip;
                this.trustclientname = trust_client_name;
                onMessageReceived.addListener(new Listener<SendMessageHandler.Message>() {
                    @Override
                    public void triggered(Message t) {
                        webChat(t.name, t.message);
                    }
                });
            }};

            plugin.webServer.handlers.put("/up/sendmessage", messageHandler);
        }*/
    }
    
    protected void webChat(String name, String message) {
        if(plugin.mapManager == null)
            return;
        // TODO: Change null to something meaningful.
        plugin.mapManager.pushUpdate(new Client.ChatMessage("web", null, name, message, null));
        Log.info(unescapeString(plugin.configuration.getString("webprefix", "\u00A72[WEB] ")) + name + ": " + unescapeString(plugin.configuration.getString("websuffix", "\u00A7f")) + message);
        ChatEvent event = new ChatEvent("web", name, message);
        plugin.events.trigger("webchat", event);
    }
}
