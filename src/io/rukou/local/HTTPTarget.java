package io.rukou.local;

import io.rukou.edge.objects.HttpRequestMessage;
import io.rukou.edge.objects.HttpResponseMessage;

public class HTTPTarget extends Target {

  @Override
  public HttpResponseMessage invoke(HttpRequestMessage msg) {
    HttpResponseMessage r = new HttpResponseMessage();
    r.requestId=msg.requestId;
    r.response.statusCode=200;
    r.messageType = "httpRequest";
    return r;
  }
}
