package io.rukou.edge.filters;

import com.google.gson.Gson;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HostFilter extends Filter {
  public static List<String> hosts;

  public HostFilter(List<String> hosts){
    this.hosts = hosts;
  }

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    Headers h = exchange.getRequestHeaders();

    if(h.containsKey("Host")){
      String hostname = h.getFirst("Host");
      if(hosts.contains(hostname.toLowerCase())){
        chain.doFilter(exchange);
      }else{
        System.out.println("rejecting unknown host "+hostname);
        Gson g = new Gson();
        System.out.println("headers "+g.toJson(exchange.getRequestHeaders()));
        exchange.sendResponseHeaders(404, 0);
        exchange.getResponseBody().close();
      }
    }else{
      System.out.println("rejecting unknown host");
      exchange.sendResponseHeaders(404, 0);
      exchange.getResponseBody().close();
    }
  }

  @Override
  public String description() {
    return "filtering the Google health check";
  }
}
