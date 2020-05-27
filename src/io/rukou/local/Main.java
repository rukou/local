package io.rukou.local;

import com.google.common.collect.Lists;
import io.rukou.local.sources.Eventhub;
import io.rukou.local.sources.Pubsub;
import io.rukou.local.sources.Source;

import java.util.Map;

public class Main {
  public static void main(String[] args) {
    Map<String, String> env = System.getenv();

    //source config
    String hostlist = env.get("SOURCE_TRUSTEDHOSTS");
    if (hostlist != null) {
      String[] hosts = hostlist.split(",");
      TrustedHosts.trustedHosts = Lists.newArrayList(hosts);
      System.out.println("applying trusted hosts");
      for (String s : hosts) {
        System.out.println("trusted host: " + s);
      }
    }

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

    if (s == null) {
      System.out.println("Source cannot be determined.");
      System.exit(1);
    } else {
      System.out.println("Rùkǒu local is running.");
      s.startAsync();
    }
  }
}
