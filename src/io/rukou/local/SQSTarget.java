package io.rukou.local;

import io.rukou.edge.objects.HttpRequestMessage;
import io.rukou.edge.objects.HttpResponseMessage;

public class SQSTarget extends Target {
  public String accessKey;
  public String secretKey;
  String requestQueueUrl;
  String responseQueueUrl;
  String requestRegion;
  String responseRegion;

  public String getResponseQueueUrl() {
    return responseQueueUrl;
  }

  public void setResponseQueueUrl(String url) {
    this.responseQueueUrl = url;
    try {
      //extract region
      responseRegion = responseQueueUrl.replace("https://sqs.", "").replaceAll("[.].*", "");
    } catch (Exception ex) {
      //ignore
      ex.printStackTrace();
    }
  }

  public String getRequestQueueUrl() {
    return requestQueueUrl;
  }

  public void setRequestQueueUrl(String url) {
    this.requestQueueUrl = url;
    try {
      //extract region
      requestRegion = requestQueueUrl.replace("https://sqs.", "").replaceAll("[.].*", "");
    } catch (Exception ex) {
      //ignore
      ex.printStackTrace();
    }
  }

  public HttpResponseMessage invoke(HttpRequestMessage msg){
    return null;
  }

}
