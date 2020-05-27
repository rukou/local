package io.rukou.local.endpoints;

import io.rukou.local.Main;
import io.rukou.local.Message;
import io.rukou.local.TrustedHosts;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;

public class Jms extends Endpoint {

  @Override
  public Message invoke(Message msg) {
    Message result = new Message();
    result.header.put("X-REQUEST-ID", msg.getRequestId());
    result.header.put("X-HTTP-STATUSCODE", "500");
    Connection connection = null;
    String providerUrl = msg.header.get("X-JMS-PROVIDERURL");
    if (TrustedHosts.IsTrustedHosts(providerUrl)) {
      try {
        String initialContextFactory = msg.header.get("X-JMS-INITIALFACTORY");
        String connectionFactoryName = msg.header.get("X-JMS-CONNECTIONFACTORY");
        String destinationName = msg.header.get("X-JMS-DESTINATION");
        String user = msg.header.get("X-JMS-USER");
        String password = msg.header.get("X-JMS-PASSWORD");
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        props.setProperty(Context.PROVIDER_URL, providerUrl);
        props.setProperty(Context.SECURITY_PRINCIPAL, user);
        props.setProperty(Context.SECURITY_CREDENTIALS, password);

        Context ctx = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(connectionFactoryName);
        Destination destination = (Destination) ctx.lookup(destinationName);

        connection = connectionFactory.createConnection(user, password);
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(destination);
        TextMessage message = session.createTextMessage();
        //prepare request reply
        Destination replyDestination;
        if (destination instanceof Queue) {
          replyDestination = session.createTemporaryQueue();
        } else {
          replyDestination = session.createTemporaryTopic();
        }
        message.setJMSReplyTo(replyDestination);
        //propagate body
        message.setText(msg.body);
        //propagate header
        for (Map.Entry<String, String> entry : msg.header.entrySet()) {
          if (!entry.getKey().startsWith("X-JMS")) {
            message.setStringProperty(entry.getKey(), entry.getValue());
          }
        }

        producer.send(message);

        //wait for response
        MessageConsumer consumer = session.createConsumer(replyDestination);

        javax.jms.Message response = consumer.receive(25000);
        if (response == null) {
          result.header.put("X-HTTP-STATUSCODE", "500");
          result.body = "request timed out.";
        } else {
          result.body = ((TextMessage) response).getText();
          result.header.put("X-HTTP-STATUSCODE", "200");
        }

      } catch (NamingException e) {
        System.out.println("JNDI lookup failed");
        e.printStackTrace();
      } catch (JMSException e) {
        System.out.println("Exception occurred: " + e);
        e.printStackTrace();
      } catch (Exception e) {
        System.out.println("Exception occurred: " + e);
        e.printStackTrace();
        System.out.println("failing message");
        for (Map.Entry<String, String> entry : msg.header.entrySet()) {
          if (!entry.getKey().startsWith("X-JMS")) {
            System.out.println("msg header: " + entry.getKey() + " = " + entry.getValue());
          }
        }
      } finally {
        if (connection != null) {
          try {
            connection.close();
          } catch (JMSException ignored) {
          }
        }
      }
    }else{
      //host is not trusted
      result.header.put("X-HTTP-STATUSCODE", "500");
      result.body = "Endpoint is not trusted";
    }
    return result;
  }
}
