package webserver;

public enum WebServerSSEEventType {
    CONNECT("connect"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe");

    public final String value;

    private WebServerSSEEventType(String value)
    {
        this.value = value;
    }
}
