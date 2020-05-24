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
import io.rukou.local.endpoints.Echo;
import io.rukou.local.endpoints.Http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Pubsub extends Source {
  String edge2localTopic;
  String edge2localSubscription;
  String local2edgeTopic;
  String serviceAccount;
  ServiceAccountCredentials account;
  CredentialsProvider credentialsProvider;
  Publisher publisher;
  ConcurrentHashMap<String, Publisher> publishers = new ConcurrentHashMap<>();

  public Pubsub(String edge2localTopic, String edge2localSubscription, String local2edgeTopic, String serviceAccount) {
    this.edge2localTopic = edge2localTopic;
    this.edge2localSubscription = edge2localSubscription;
    this.local2edgeTopic = local2edgeTopic;
    this.serviceAccount = serviceAccount;

    InputStream stream = new ByteArrayInputStream(serviceAccount.getBytes(StandardCharsets.UTF_8));
    try {
      account = ServiceAccountCredentials.fromStream(stream);
      credentialsProvider = FixedCredentialsProvider.create(account);
      publisher = Publisher.newBuilder(local2edgeTopic).setCredentialsProvider(() -> account).build();
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

          //running against endpoint
          String type = msg.getEndpointType();
          Message result;
          switch (type) {
            case "echo":
              Echo echoEndpoint = new Echo();
              result = echoEndpoint.invoke(msg);
              break;
            case "http":
              Http httpEndpoint = new Http();
              result = httpEndpoint.invoke(msg);
              break;
            default:
              System.err.println("endpoint cannot be determined, falling back to 'echo'");
              Echo defaultEndpoint = new Echo();
              result = defaultEndpoint.invoke(msg);
              break;
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
          publisher.publish(pubsubMessage);

          System.out.println("replied " + requestId + " replying to " + local2edgeDestination);

          consumer.ack();
        };

    Subscriber subscriber = null;
    try {
      // Create a subscriber for "my-subscription-id" bound to the message receiver
      subscriber = Subscriber.newBuilder(edge2localSubscription, receiver).setCredentialsProvider(() -> account).build();
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

  public void pushReply(Message msg) {
  }
}
