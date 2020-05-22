package io.rukou.local;

import io.rukou.edge.objects.HttpRequestMessage;
import io.rukou.edge.objects.HttpResponseMessage;

public abstract class Target {
  public abstract HttpResponseMessage invoke(HttpRequestMessage msg);
}
