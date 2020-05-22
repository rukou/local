package io.rukou.edge;

import io.rukou.edge.objects.HttpRequestMessage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.rukou.edge.routes.Route;
import io.rukou.edge.routes.SQSRoute;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RequestHandler implements HttpHandler {

  List<Route> routes;

  public RequestHandler(List<Route> routes) {
    this.routes = routes;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      HttpRequestMessage msg = new HttpRequestMessage(exchange);

      String response = "ok";

      //forward to route
      for (Route r : routes) {
        if (r instanceof SQSRoute) {
          response = r.invoke(msg);
        }
      }
      if (response == null) {
        response = "";
      }
      byte[] out = response.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, out.length);
      OutputStream os = exchange.getResponseBody();
      os.write(out);
      os.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      exchange.sendResponseHeaders(500, 0);
      exchange.getResponseBody().close();
    }
  }
}
