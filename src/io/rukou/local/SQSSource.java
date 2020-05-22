package io.rukou.local;

import io.rukou.edge.objects.Message;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

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

  public Message pollSource(){
    SqsClient sqsClient = SqsClient.builder()
        .region(Region.of(sourceRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                new AwsCredentials(){
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
    sqsClient.receiveMessage(receiveMessageRequest).messages();
    Message m=null;
    return m;
  }

  public void pushReply(Message msg){
    String jsonMsg = msg.toJson();

    SqsClient sqsClient = SqsClient.builder()
        .region(Region.of(replyRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                new AwsCredentials(){
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
    SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
        .queueUrl(replyQueueUrl)
        .messageBody(jsonMsg)
        .build());
  }
}
