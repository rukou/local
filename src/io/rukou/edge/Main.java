package io.rukou.edge;

import io.rukou.edge.filters.HealthCheckFilter;
import io.rukou.edge.filters.HostFilter;
import com.sun.net.httpserver.*;
import io.rukou.edge.routes.Route;
import io.rukou.edge.routes.SQSRoute;
import org.yaml.snakeyaml.Yaml;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
  public static int port = 8080;

  public static void main(String[] args) throws Exception {
    if (System.getenv().containsKey("port")) {
      port = Integer.parseInt(System.getenv().get("port"));
    }
    if (System.getenv().containsKey("PORT")) {
      port = Integer.parseInt(System.getenv().get("PORT"));
    }

    //get config map from env
    Yaml y = new Yaml();
    Map<String, String> e = System.getenv();

    //EDGE configuration
    String edgeConfig = e.get("EDGECONFIG");
    List<String> hosts = new ArrayList<>();
    if (edgeConfig != null && !edgeConfig.isEmpty()) {
      Map<String, Object> x = y.load(edgeConfig);

      //get hosts

      Object t = x.get("hosts");
      if (t instanceof String) hosts = new ArrayList<String>() {{
        add(t.toString());
      }};
      if (t instanceof List) hosts = (List<String>) t;
    }

    //ROUTES configuration
    String routesConfig = e.get("ROUTESCONFIG");
    List<Route> routes = new ArrayList<>();
    if (routesConfig != null && !routesConfig.isEmpty()) {
      Map<String, Object> x = y.load(routesConfig);
      for (String routeName : x.keySet()) {
        Map<String, Object> routeConfig = (Map<String, Object>)x.get(routeName);
        String routeType = (String) routeConfig.get("type");
        if(routeType!=null && routeType.equals("aws-sqs")){
          SQSRoute route = new SQSRoute();
          route.format = routeConfig.getOrDefault("format","json").toString();
          route.accessKey = routeConfig.getOrDefault("accessKey","").toString();
          route.secretKey = routeConfig.getOrDefault("secretKey","").toString();
          route.setRequestQueueUrl(routeConfig.getOrDefault("requestQueueUrl","").toString());
          route.setResponseQueueUrl(routeConfig.getOrDefault("responseQueueUrl","").toString());
          routes.add(route);
        }
      }
    }

    //start server
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 100);
    HttpContext all = server.createContext("/", new RequestHandler(routes));
    all.getFilters().add(new HealthCheckFilter());
    if (hosts.size() > 0) {
      all.getFilters().add(new HostFilter(hosts));
    }

    System.out.println("rukou edge is running.");
    System.out.println("http://localhost:" + port + "/");
    server.start();
  }
}
