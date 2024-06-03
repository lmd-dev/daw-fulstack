package webserver;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WebServerSSE {

    private WebServerRouter router;

    private HashMap<String, OutputStream> clientStreams;
    private HashMap<String, ArrayList<String>> channels;

    private HashMap<String, ArrayList<WebServerSSEEventHandler>> eventListeners;

    public WebServerSSE(WebServerRouter router) {
        this.router = router;

        this.clientStreams = new HashMap<>();
        this.channels = new HashMap<>();
        this.eventListeners = new HashMap<>();

        this.init();
    }

    private void init() {
        router.get("/__sse/:clientId", this::connect);
        router.post("/__sse/:clientId/channel/:channel", this::subscribe);
        router.delete("/__sse/:clientId/channel/:channel", this::unsubscribe);
    }

    private void connect(WebServerContext context) {
        String clientId = context.getRequest().getParam("clientId");
        OutputStream outputStream = context.getResponse().openSSEStream();

        clientStreams.put(clientId, outputStream);

        dispatchEvent(WebServerSSEEventType.CONNECT, new WebServerSSEEvent(WebServerSSEEventType.CONNECT, clientId, null));
    }

    private void subscribe(WebServerContext context) {
        String clientId = context.getRequest().getParam("clientId");
        String channelName = context.getRequest().getParam("channel");

        if (this.clientStreams.get(clientId) == null) {
            context.getResponse().notFound("Unknown Client ID");
            return;
        }

        ArrayList<String> channelUsers = this.channels.get(channelName);

        if (channelUsers == null) {
            channelUsers = new ArrayList<String>();
            this.channels.put(channelName, channelUsers);
        } else if (channelUsers.contains(clientId)) {
            context.getResponse().ok("Already subscribed to this channel");
            return;
        }

        channelUsers.add(clientId);

        context.getResponse().ok("");

        dispatchEvent(WebServerSSEEventType.SUBSCRIBE, new WebServerSSEEvent(WebServerSSEEventType.SUBSCRIBE, clientId, channelName));
    }

    private void unsubscribe(WebServerContext context) {
        String clientId = context.getRequest().getParam("clientId");
        String channelName = context.getRequest().getParam("channel");

        if (this.clientStreams.get(clientId) == null) {
            context.getResponse().notFound("Unknown Client ID");
            return;
        }

        ArrayList<String> channelUsers = this.channels.get(channelName);

        if (channelUsers == null) {
            context.getResponse().notFound("Unknown Channel");
            return;
        }

        if (channelUsers.contains(clientId) == false) {
            context.getResponse().notFound("Not subscribe to this channel");
            return;
        }

        channelUsers.remove(clientId);

        context.getResponse().ok("");

        dispatchEvent(WebServerSSEEventType.UNSUBSCRIBE, new WebServerSSEEvent(WebServerSSEEventType.UNSUBSCRIBE, clientId, channelName));
    }

    public void emit(String channel, Object data) {
        ArrayList<String> channelUsers = this.channels.get(channel);

        if (channelUsers == null)
            return;

        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.create();

        for (String clientId : channelUsers) {
            this.emit(this.clientStreams.get(clientId), channel, gson.toJson(data));
        }
    }

    private void emit(OutputStream stream, String channel, String message) {
        if (stream == null)
            return;

        try {
            stream.write(String.format("event: %s\n", channel).getBytes());
            stream.write(String.format("data: %s", message).getBytes());
            stream.write("\n\n".getBytes());
            stream.flush();
        } catch (Exception e) {

        }
    }

    public void addEventListeners(WebServerSSEEventType eventType, WebServerSSEEventHandler handler)
    {
        if(this.eventListeners.containsKey(eventType.value) == false)
            this.eventListeners.put(eventType.value, new ArrayList<WebServerSSEEventHandler>());

        this.eventListeners.get(eventType.value).add(handler);
    }

    private void dispatchEvent(WebServerSSEEventType eventType, WebServerSSEEvent event)
    {
        ArrayList<WebServerSSEEventHandler> handlers = this.eventListeners.get(eventType.value);

        if(handlers == null)
            return;

        for(WebServerSSEEventHandler handler: handlers)
        {
            handler.run(event);
        }
    }
}
