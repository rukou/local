package io.rukou.local.endpoints;

import io.rukou.local.Message;

public class Echo extends Endpoint {

  @Override
  public Message invoke(Message msg) {
    Message r = new Message();
    r.body=msg.body;
    r.header=msg.header;
    r.header.put("X-HTTP-STATUSCODE","200");
    return r;
  }
}
