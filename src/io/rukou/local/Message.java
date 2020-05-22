package io.rukou.local;

import java.util.HashMap;
import java.util.Map;

public class Message {
  public Map<String,String> header = new HashMap<>();
  public String body="";

  public String getMessageType(){
    return header.getOrDefault("X-MESSAGE-TYPE","");
  }
  public String getMessageVersion(){
    return header.getOrDefault("X-MESSAGE-VERSION","");
  }
  public String getRequestId(){
    return header.getOrDefault("X-REQUEST-ID","");
  }

  public String getEndpointType(){
    return header.getOrDefault("X-ENDPOINT-TYPE","");
  }

  public String toString(){
    return "body: "+body;
  }
}
