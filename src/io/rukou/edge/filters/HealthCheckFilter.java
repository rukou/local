package io.rukou.edge.filters;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HealthCheckFilter extends Filter {
  final String userAgent="User-agent";

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    Headers h = exchange.getRequestHeaders();

    if(h.containsKey(userAgent) && h.getFirst(userAgent).equals("GoogleHC/1.0")){
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    }else{
      chain.doFilter(exchange);
    }
  }

  @Override
  public String description() {
    return "filtering the Google health check";
  }
}
