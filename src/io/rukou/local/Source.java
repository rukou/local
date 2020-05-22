package io.rukou.local;

public abstract class Source {
  public abstract Message pollSource();
  public abstract void pushReply(Message msg);
}
