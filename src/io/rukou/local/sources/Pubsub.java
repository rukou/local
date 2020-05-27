package io.rukou.local.sources;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.rukou.local.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Pubsub extends Source {
  String edge2localSubscription;
  String serviceAccount;
  CredentialsProvider credentialsProvider;
  ConcurrentHashMap<String, Publisher> publishers = new ConcurrentHashMap<>();

  public Pubsub(String edge2localSubscription, String serviceAccount) {
    this.edge2localSubscription = edge2localSubscription;
    this.serviceAccount = serviceAccount;
    InputStream stream = new ByteArrayInputStream(serviceAccount.getBytes(StandardCharsets.UTF_8));
    try {
      ServiceAccountCredentials account = ServiceAccountCredentials.fromStream(stream);
      credentialsProvider = FixedCredentialsProvider.create(account);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Message pollSource() {
    return null;
  }

  public void startAsync() {
    MessageReceiver receiver =
        (message, consumer) -> {
          Message msg = new Message();
          //copy all header values
          Map<String, String> headers = message.getAttributesMap();
          for (Map.Entry<String, String> entry : headers.entrySet()) {
            msg.header.put(entry.getKey(), entry.getValue());
          }
          msg.body = message.getData().toStringUtf8();

          String local2edgeDestination = msg.header.get("X-LOCAL2EDGE-DESTINATION");
          String requestId = msg.getRequestId();
          Message result;
          try {
            result = this.processMessage(msg);
          } catch (Exception ex) {
            ex.printStackTrace();
            result = new Message();
            result.header.put("X-REQUEST-ID", msg.getRequestId());
            result.header.put("X-HTTP-STATUSCODE", "500");
          }
          //reply message
          PubsubMessage pubsubMessage =
              PubsubMessage.newBuilder().setData(ByteString
                  .copyFromUtf8(result.body)).putAllAttributes(result.header).build();
          Publisher publisher = publishers.computeIfAbsent(local2edgeDestination, (x) -> {
                try {
                  return Publisher.newBuilder(x).setCredentialsProvider(credentialsProvider).build();
                } catch (IOException e) {
                  System.out.println("could not create publisher for destination " + x);
                  e.printStackTrace();
                  return null;
                }
              }
          );
          if (publisher != null) {
            publisher.publish(pubsubMessage);
            System.out.println("replied " + requestId + " replying to " + local2edgeDestination);
          }
          consumer.ack();
        };

    Subscriber subscriber = null;
    try {
      // Create a subscriber for "my-subscription-id" bound to the message receiver
      subscriber = Subscriber.newBuilder(edge2localSubscription, receiver).setCredentialsProvider(credentialsProvider).build();
      subscriber.startAsync().awaitRunning();
      // Allow the subscriber to run indefinitely unless an unrecoverable error occurs
      subscriber.awaitTerminated();
    } finally {
      // Stop receiving messages
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
  }
}
