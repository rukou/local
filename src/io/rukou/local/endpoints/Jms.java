package io.rukou.local.endpoints;

import io.rukou.local.EnvClassLoader;
import io.rukou.local.Message;

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
import java.util.Properties;

public class Jms extends Endpoint {

  @Override
  public Message invoke(Message msg) {
    EnvClassLoader ecl = new EnvClassLoader();
    Thread.currentThread().setContextClassLoader(ecl);

    String initialContextFactory = msg.header.get("X-JMS-INITIALFACTORY");
    String providerUrl = msg.header.get("X-JMS-PROVIDERURL");
    String destinationName = msg.header.get("X-JMS-DESTINATION");
    String user = msg.header.get("X-JMS-USER");
    String password = msg.header.get("X-JMS-PASSWORD");
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
    props.setProperty(Context.PROVIDER_URL, providerUrl);
    Connection connection = null;

    Message result = new Message();
    result.header.put("X-REQUEST-ID", msg.getRequestId());
    result.header.put("X-HTTP-STATUSCODE", "500");

    try {
      Context ctx = new InitialContext(props);
      ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
      Destination destination = (Destination) ctx.lookup(destinationName);

      connection = connectionFactory.createConnection(user,password);
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(destination);
      TextMessage message = session.createTextMessage();
      //prepare request reply
      Destination replyDestination = null;
      if (destination instanceof Queue) {
        replyDestination = session.createTemporaryQueue();
        message.setJMSReplyTo(replyDestination);
      } else {
        replyDestination = session.createTemporaryTopic();
        message.setJMSReplyTo(replyDestination);
      }
      //propagate body
      message.setText(msg.body);
      //propagate header

      producer.send(message);

      //wait for resonse
      MessageConsumer consumer = session.createConsumer(replyDestination);
      javax.jms.Message response = consumer.receive(30000);
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
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (JMSException ignored) {
        }
      }
    }
    return result;
  }
}
