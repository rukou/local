package io.rukou.edge.objects;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpRequestMessage extends Message{
  public HttpRequest request;

  public class HttpRequest {
    public String method;
    public String host;
    public String path;
    public Headers headers;
  }

  public HttpRequestMessage(HttpExchange exchange){
    messageType = "httpRequest";
    request = new HttpRequest();
    request.path = exchange.getRequestURI().getPath();
    request.method = exchange.getRequestMethod();
    Headers headers = exchange.getRequestHeaders();
    for(Map.Entry<String, List<String>> entry : headers.entrySet()){
      String keyName = entry.getKey().toLowerCase();
      switch (keyName){
        case "host": request.host=entry.getValue().get(0);
      }
    }
    request.headers = headers;

  }
}
