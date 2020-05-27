package io.rukou.local.endpoints;

import io.rukou.local.Message;
import io.rukou.local.TrustedHosts;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Kafka extends Endpoint {

  @Override
  public Message invoke(Message msg) {
    Message response = new Message();
    response.header.put("X-REQUEST-ID", msg.getRequestId());
    response.header.put("X-HTTP-STATUSCODE", "500");

    Properties props = new Properties();
    String bootstrapServer = msg.header.get("X-KAFKA-BOOTSTRAPSERVERS");
    if (TrustedHosts.IsTrustedHosts(bootstrapServer)) {
      props.put("bootstrap.servers", bootstrapServer);
      props.put("acks", "all");
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

      String topic = msg.header.get("X-KAFKA-TOPIC");
      KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
      for (int i = 0; i < 100; i++)
        producer.send(new ProducerRecord<>(topic, Integer.toString(i), Integer.toString(i)));

      producer.close();
      response.header.put("X-HTTP-STATUSCODE", "200");
    } else {
      //host is not trusted
      response.header.put("X-HTTP-STATUSCODE", "500");
      response.body = "Endpoint is not trusted";
    }
    return response;
  }
}
