package io.rukou.local.endpoints;

import io.rukou.local.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Http extends Endpoint {

  @Override
  public Message invoke(Message msg) {
    String endpoint = msg.header.get("X-HTTP-ENDPOINT");
    String method = msg.header.get("X-HTTP-METHOD");
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
    for (Map.Entry<String, String> entry : msg.header.entrySet()) {
      String key = entry.getKey();
      //ignore problematic header keys
      switch (key){
        case "HOST":
        case "CONNECTION":
        case "CONTENT-LENGTH":
          continue;
        default:
          requestBuilder.header(entry.getKey(), entry.getValue());
          break;
      }
    }
    requestBuilder.uri(URI.create(endpoint))
        .method(method, HttpRequest.BodyPublishers.ofString(msg.body));
    HttpRequest request = requestBuilder.build();
    HttpClient client = HttpClient.newBuilder().build();
    Message r = new Message();
    r.header.put("X-REQUEST-ID", msg.getRequestId());
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      r.header.put("X-HTTP-STATUSCODE", String.valueOf(response.statusCode()));
      r.body = response.body();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.out.println("http handler " + msg.getRequestId() + " for " + endpoint);
    return r;
  }
}
