package io.rukou.local;

import java.util.ArrayList;

public class TrustedHosts {
  public static ArrayList<String> trustedHosts;

  public static boolean IsTrustedHosts(String hostName) {
    if (trustedHosts == null) {
      return true;
    } else {
      for (String host : trustedHosts) {
        if (host.contains(hostName)) {
          return true;
        }
        if (hostName.contains(host)) {
          return true;
        }
      }
      return false;
    }
  }

}
