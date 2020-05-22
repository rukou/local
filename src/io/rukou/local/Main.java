package io.rukou.local;

import io.rukou.local.endpoints.Echo;
import io.rukou.local.endpoints.Http;

import java.util.Map;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Map<String, String> env = System.getenv();

    //source config
    String sourceType = env.get("SOURCE_TYPE");
    if(sourceType == null){
      System.err.println("missing source definition");
      System.exit(1);
    }
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
    Target t=null;
    switch (targetType){
      case "http":
        t = new Http();
        break;
      case "echo":
        t = new Echo();
        break;
    }

    System.out.println("Rùkǒu local is running.");
    while(true){
      Message msg = s.pollSource();
      if(msg == null){
        Thread.sleep(1000);

      }else {
        String endpointType = msg.getEndpointType();

        System.out.println(msg.toString());
        Message respMsg = t.invoke(msg);
        s.pushReply(respMsg);
      }
    }
  }
}
