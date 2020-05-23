package io.rukou.local.sources;

import io.rukou.local.Message;

public abstract class Source {
  public abstract Message pollSource();
  public abstract void pushReply(Message msg);
}
