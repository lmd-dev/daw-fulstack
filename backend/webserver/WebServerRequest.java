package webserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class WebServerRequest {
    private HttpExchange exchange;
    private final HashMap<String, String> params;
    private Object body;

    WebServerRequest(HttpExchange exchange) {
        this.exchange = exchange;
        this.params = new HashMap<>();
        this.body = null;
    }

    public String getMethod() {
        return this.exchange.getRequestMethod();
    }

    public String getPath() {
        return this.exchange.getRequestURI().getPath();
    }

    public void setParams(HashMap<String, String> params) {
        this.params.clear();
        this.params.putAll(params);
    }

    public String getParam(String key) {
        return this.params.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T extractBody(Class<T> type) {
        if(body == null)
        {
            Headers headers = exchange.getRequestHeaders();
            String contentType = headers.getFirst("Content-Type");

            if (contentType.equals("application/json")) 
            {
                String bodyAsString = this.readStreamAsString();

                final GsonBuilder builder = new GsonBuilder();
                final Gson gson = builder.create();

                this.body = gson.fromJson(bodyAsString, type);
            }
        }

        return (T)this.body;
    }

    private String readStreamAsString()
    {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);

            int character;
            StringBuilder buffer = new StringBuilder(512);
            while ((character = bufferReader.read()) != -1) {
                buffer.append((char) character);
            }

            bufferReader.close();
            inputStreamReader.close();

            return buffer.toString();
        }
        catch(Exception e)
        {
        
        }

        return "";
    }
}
