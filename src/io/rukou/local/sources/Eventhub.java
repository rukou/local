package io.rukou.local.sources;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.PartitionContext;
import io.rukou.local.Message;
import reactor.core.Disposable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Eventhub extends Source {
  String eventhubNamespaceUrl;
  ConcurrentHashMap<String, EventHubProducerClient> producer = new ConcurrentHashMap<>();

  public Eventhub(String eventhubNamespaceUrl) {
    this.eventhubNamespaceUrl = eventhubNamespaceUrl;
  }

  public void startAsync() {
    EventHubProducerClient producer = new EventHubClientBuilder()
        .connectionString(eventhubNamespaceUrl + ";EntityPath=local2edge")
        .buildProducerClient();

    EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
        .connectionString(eventhubNamespaceUrl + ";EntityPath=edge2local")
        .consumerGroup("$Default")
        .buildAsyncConsumerClient();

    Disposable subscription = consumer.receive(false)
        .subscribe(partitionEvent -> {
              PartitionContext partitionContext = partitionEvent.getPartitionContext();
              EventData event = partitionEvent.getData();

              Message msg = new Message();
              for (Map.Entry<String, Object> entry : event.getProperties().entrySet()) {
                msg.header.put(entry.getKey(), entry.getValue().toString());
              }
              msg.body = event.getBodyAsString();

              String requestId = msg.getRequestId();
              System.out.println("received " + requestId);

              Message result = this.processMessage(msg);

              EventData eventData = new EventData(result.body);
              eventData.getProperties().putAll(result.header);

              List<EventData> events = Arrays.asList(eventData);
              producer.send(events);
            }
        );
    while(true){
      try {
        Thread.currentThread().sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
