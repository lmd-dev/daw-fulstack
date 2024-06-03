package webserver;

import java.util.ArrayList;

public class WebServerRouter {
    private ArrayList<WebServerRoute> routes;

    public WebServerRouter()
    {
        this.routes = new ArrayList<>();
    }

    public void get(String path, WebServerRouteHandler handler)
    {
        this.addRoute("GET", path, handler);
    }

    public void post(String path, WebServerRouteHandler handler)
    {
        this.addRoute("POST", path, handler);
    }

    public void put(String path, WebServerRouteHandler handler)
    {
        this.addRoute("PUT", path, handler);
    }

    public void delete(String path, WebServerRouteHandler handler)
    {
        this.addRoute("DELETE", path, handler);
    }

    private void addRoute(String method, String path, WebServerRouteHandler handler)
    {
        this.routes.add(new WebServerRoute(method, path, handler));
    }

    public WebServerRoute findRoute(WebServerRequest request) throws WebServerRouteNotFoundException
    {
        String method = request.getMethod();
        String path = request.getPath();

        for(WebServerRoute route: this.routes)
        {
            if(route.match(method, path))
                return route;
        }

        throw new WebServerRouteNotFoundException(method, path);
    }
}
