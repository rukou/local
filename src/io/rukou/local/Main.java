package io.rukou.local;

import io.rukou.edge.objects.HttpRequestMessage;
import io.rukou.edge.objects.HttpResponseMessage;
import io.rukou.edge.objects.Message;

import java.util.Map;

public class Main {
  public static void main(String[] args){
    Map<String, String> env = System.getenv();

    //source config
    String sourceType = env.get("SOURCE_TYPE");
    String sourceFormat = env.get("SOURCE_FORMAT");
    Source s=null;
    switch (sourceType){
      case "aws-sqs":
        SQSSource x=new SQSSource();
        x.accessKey=env.get("SOURCE_ACCESSKEY");
        x.secretKey=env.get("SOURCE_SECRETKEY");
        x.setRequestQueueUrl(env.get("SOURCE_REQUESTQUEUEURL"));
        x.setReplyQueueUrl(env.get("SOURCE_RESPONSEQUEUEURL"));
        s=x;
        break;
    }

    //target config
    String targetType = env.get("TARGET_TYPE");
    String targetFormat = env.get("TARGET_FORMAT");
    Target t=null;
    switch (targetType){
      case "aws-sqs":
        SQSTarget x=new SQSTarget();
        x.accessKey=env.get("TARGET_ACCESSKEY");
        x.secretKey=env.get("TARGET_SECRETKEY");
        x.setRequestQueueUrl(env.get("TARGET_REQUESTQUEUEURL"));
        x.setResponseQueueUrl(env.get("TARGET_RESPONSEQUEUEURL"));
        t=x;
        break;
      case "http":
        t = new HTTPTarget();
        break;
    }

    System.out.println("rukou local is running.");
    while(true){
      Message msg = s.pollSource();
      HttpRequestMessage reqMsg=null;
      HttpResponseMessage respMsg = t.invoke(reqMsg);
      s.pushReply(respMsg);
    }
  }
}
