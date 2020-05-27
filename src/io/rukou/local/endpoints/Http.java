package io.rukou.local.endpoints;

import io.rukou.local.Main;
import io.rukou.local.Message;
import io.rukou.local.TrustedHosts;

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
      switch (key) {
        case "HOST":
        case "CONNECTION":
        case "CONTENT-LENGTH":
          continue;
        default:
          requestBuilder.header(entry.getKey(), entry.getValue());
          break;
      }
    }
    URI uri = URI.create(endpoint);
    Message r = new Message();
    r.header.put("X-REQUEST-ID", msg.getRequestId());
    if (TrustedHosts.IsTrustedHosts(uri.getHost())) {
      requestBuilder.uri(URI.create(endpoint))
          .method(method, HttpRequest.BodyPublishers.ofString(msg.body));
      HttpRequest request = requestBuilder.build();
      HttpClient client = HttpClient.newBuilder().build();

      try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        r.header.put("X-HTTP-STATUSCODE", String.valueOf(response.statusCode()));
        r.body = response.body();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } else {
      //host is not trusted
      r.header.put("X-HTTP-STATUSCODE", "500");
      r.body = "Endpoint is not trusted";
    }
    System.out.println("http handler " + msg.getRequestId() + " for " + endpoint);
    return r;
  }
}
