package webserver;

import java.util.HashMap;

public class WebServerRoute {
    private String  method;
    private String path;
    private WebServerRouteHandler handler;

    public WebServerRoute(String method, String path, WebServerRouteHandler handler)
    {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }

    public void run(WebServerContext context)
    {
        this.handler.run(context);
    }

    public boolean match(String method, String path)
    {
        if(method.equals(this.method) == false)
            return false;

        return this.comparePath(this.path, path);
    }

    private boolean comparePath(String routePath, String requestPath)
    {
        String[] routePathParts = routePath.split("/");
        String[] requestPathParts = requestPath.split("/");

        if(routePathParts.length != requestPathParts.length)
            return false;

        for(int i = 0; i < routePathParts.length; ++i)
        {
            String routePart = routePathParts[i];
            String requestPart = requestPathParts[i];

            if(routePart.length() != 0 && routePart.charAt(0) == ':')
                continue;

            if(routePart.equals(requestPart) == false)
                return false;
        }

        return true;
    }

    public HashMap<String, String> extractParams(String requestPath)
    {
        HashMap<String, String> params = new HashMap<>();

        String[] routePathParts = this.path.split("/");
        String[] requestPathParts = requestPath.split("/");

        if(routePathParts.length == requestPathParts.length)
        {
            for(int i = 0; i < routePathParts.length; ++i)
            {
                String routePart = routePathParts[i];
                String requestPart = requestPathParts[i];

                if(routePart.length() != 0 && routePart.charAt(0) == ':')
                {
                    params.put(routePart.substring(1, routePart.length()), requestPart);
                }
            }
        }

        return params;
    }
}
