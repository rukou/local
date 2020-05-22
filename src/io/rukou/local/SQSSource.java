package io.rukou.local;

import io.rukou.local.Message;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQSSource extends Source {
  public String accessKey;
  public String secretKey;
  String sourceQueueUrl;
  String replyQueueUrl;
  String sourceRegion;
  String replyRegion;

  public String getReplyQueueUrl() {
    return replyQueueUrl;
  }

  public void setReplyQueueUrl(String url) {
    this.replyQueueUrl = url;
    try {
      //extract region
      replyRegion = replyQueueUrl.replace("https://sqs.", "").replaceAll("[.].*", "");
    } catch (Exception ex) {
      //ignore
      ex.printStackTrace();
    }
  }

  public String getSourceQueueUrl() {
    return sourceQueueUrl;
  }

  public void setRequestQueueUrl(String url) {
    this.sourceQueueUrl = url;
    try {
      //extract region
      sourceRegion = sourceQueueUrl.replace("https://sqs.", "").replaceAll("[.].*", "");
    } catch (Exception ex) {
      //ignore
      ex.printStackTrace();
    }
  }

  public Message pollSource() {
    SqsClient sqsClient = SqsClient.builder()
        .region(Region.of(sourceRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                new AwsCredentials() {
                  @Override
                  public String accessKeyId() {
                    return accessKey;
                  }

                  @Override
                  public String secretAccessKey() {
                    return secretKey;
                  }
                }))
        .build();
    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
        .queueUrl(sourceQueueUrl)
        .maxNumberOfMessages(1)
        .build();
    List<software.amazon.awssdk.services.sqs.model.Message> all = sqsClient.receiveMessage(receiveMessageRequest).messages();
    if (all.size() > 0) {
      software.amazon.awssdk.services.sqs.model.Message sqsMsg = all.get(0);
      String body = sqsMsg.body();
      Map<String, String> header = sqsMsg.attributesAsStrings();
      sqsClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(sourceQueueUrl).receiptHandle(sqsMsg.receiptHandle()).build());
      io.rukou.local.Message msg = new io.rukou.local.Message();
      msg.header = header;
      msg.body = body;
      return msg;
    } else {
      return null;
    }
  }

  public void pushReply(Message msg) {
    SqsClient sqsClient = SqsClient.builder()
        .region(Region.of(replyRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                new AwsCredentials() {
                  @Override
                  public String accessKeyId() {
                    return accessKey;
                  }

                  @Override
                  public String secretAccessKey() {
                    return secretKey;
                  }
                }))
        .build();
    Map<String, MessageAttributeValue> sqsHeader = new HashMap<>();
    msg.header.forEach((key, val) -> {
      sqsHeader.put(key, MessageAttributeValue.builder().stringValue(val).build());
    });
    System.out.println("providing body " + msg.body);
    SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
        .queueUrl(replyQueueUrl)
        .messageAttributes(sqsHeader)
        .messageBody("hallo")
        .build());
    System.out.println(response.messageId());
  }
}
