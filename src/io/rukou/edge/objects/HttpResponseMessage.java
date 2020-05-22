package io.rukou.edge.objects;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.util.List;
import java.util.Map;

public class HttpResponseMessage extends Message{
  public HttpResponse response;

  public class HttpResponse {
    public int statusCode;
    public Headers headers;
  }
}
