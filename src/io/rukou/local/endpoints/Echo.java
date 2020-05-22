package io.rukou.local.endpoints;

import io.rukou.local.Message;
import io.rukou.local.Target;

public class Echo extends Target {

  @Override
  public Message invoke(Message msg) {
    Message r = new Message();
    r.body=msg.body;
    r.header=msg.header;
    return r;
  }
}
