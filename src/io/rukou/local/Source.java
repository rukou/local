package io.rukou.local;

import io.rukou.edge.objects.Message;

public abstract class Source {
  public abstract Message pollSource();
  public abstract void pushReply(Message msg);
}
