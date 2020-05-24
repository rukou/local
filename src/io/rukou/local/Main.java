package io.rukou.local;

import io.rukou.local.sources.Pubsub;
import io.rukou.local.sources.Source;

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
        String edge2localSubscription = env.get("SOURCE_EDGE2LOCALSUBSCRIPTION");
        String serviceAccount = env.get("SOURCE_SERVICEACCOUNT");
        Pubsub pubsub = new Pubsub(edge2localSubscription, serviceAccount);
        s = pubsub;
        break;
    }

    System.out.println("Rùkǒu local is running.");
    if(s instanceof Pubsub){
      ((Pubsub)s).startAsync();
    }
  }
}
