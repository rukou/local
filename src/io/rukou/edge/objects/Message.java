package io.rukou.edge.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.util.UUID;

public abstract class Message {
  public String requestId = UUID.randomUUID().toString();
  public String messageType;
  public String messageVersion="2020-05-21";

  public String toJson(){
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public String toXml(){
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "";
    }
  }
}
