package io.rukou.local.endpoints;

import io.rukou.local.Message;

public abstract class Endpoint {
  public abstract Message invoke(Message msg);
}
