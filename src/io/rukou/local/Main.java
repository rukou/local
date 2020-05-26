package io.rukou.local;

import io.rukou.local.sources.Eventhub;
import io.rukou.local.sources.Pubsub;
import io.rukou.local.sources.Source;

import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Provider;
import java.util.Map;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Map<String, String> env = System.getenv();

    //source config
    String sourceType = env.get("SOURCE_TYPE");
    if (sourceType == null) {
      System.err.println("missing source definition");
      System.exit(1);
    }
    Source s = null;
    switch (sourceType) {
      case "google-pubsub":
        String edge2localSubscription = env.get("SOURCE_PUBSUB_SUBSCRIPTION");
        String serviceAccount = env.get("SOURCE_PUBSUB_SERVICEACCOUNT");
        Pubsub pubsub = new Pubsub(edge2localSubscription, serviceAccount);
        s = pubsub;
        break;
      case "azure-eventhub":
        String edge2localNamespace = env.get("SOURCE_EVENTHUB_URL");
        Eventhub eventhub = new Eventhub(edge2localNamespace);
        s = eventhub;
        break;
    }

    System.out.println("Rùkǒu local is running.");
    s.startAsync();
  }
}
