package webserver;

public record WebServerSSEEvent(
    WebServerSSEEventType type,
    String clientId,
    String channel
) {}
