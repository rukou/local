package io.rukou.local.sources;

import io.rukou.local.Message;
import io.rukou.local.endpoints.*;

public abstract class Source {

  public abstract void startAsync();

  protected Message processMessage(Message msg){
    //running against endpoint
    String type = msg.getEndpointType();
    Message result=null;
    switch (type) {
      case "echo":
        Echo echoEndpoint = new Echo();
        result = echoEndpoint.invoke(msg);
        break;
      case "http":
        Http httpEndpoint = new Http();
        result = httpEndpoint.invoke(msg);
        break;
      case "jms":
        Endpoint jmsEndpoint = new Jms();
        result = jmsEndpoint.invoke(msg);
        break;
      case "kafka":
        Endpoint kafkaEndpoint = new Kafka();
        result = kafkaEndpoint.invoke(msg);
        break;
      default:
        System.err.println("endpoint cannot be determined, falling back to 'echo'");
        Echo defaultEndpoint = new Echo();
        result = defaultEndpoint.invoke(msg);
        break;
    }
    return result;
  }
}
