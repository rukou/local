package io.rukou.edge.routes;

import io.rukou.edge.objects.Message;

public abstract class Route {
  public String type;
  public String format = "json";

  public abstract String invoke(Message msg);
}
