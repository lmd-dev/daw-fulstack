package webserver;

import com.sun.net.httpserver.HttpExchange;

public class WebServerContext {
    private WebServerRequest request;
    private WebServerResponse response;
    private WebServerSSE sse;

    WebServerContext(HttpExchange exchange, WebServerSSE sse)
    {
        this.request = new WebServerRequest(exchange);
        this.response = new WebServerResponse(exchange);
        this.sse = sse;
    }

    public WebServerRequest getRequest() {
        return request;
    }

    public WebServerResponse getResponse() {
        return response;
    }

    public WebServerSSE getSSE() {
        return sse;
    }
}
