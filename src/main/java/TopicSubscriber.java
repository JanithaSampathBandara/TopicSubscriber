
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class TopicSubscriber {

    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static String userName = "admin";
    private static String password = "admin";
    private static String CARBON_CLIENT_ID = "carbon";
    private static String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static String serverHostname = "localhost";
    private static String jmsPort = "5672";
    private static String topicName = "notification";
    private static TopicConnection topicConnection;
    private static TopicSession topicSession;
    private static javax.jms.TopicSubscriber topicSubscriber;
    private static final long DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

    public static void main(String[] args) throws NamingException, JMSException {

        try {

            // Create a timer to stop the program after a specified duration
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        System.out.println("Subscriber has run for 5 minutes. Stopping...");
                        // Housekeeping
                        topicSubscriber.close();
                        topicSession.close();
                        topicConnection.stop();
                        topicConnection.close();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    } finally {
                        System.exit(0);
                    }
                }
            }, DURATION);

            String path = args[0];
            File file = new File(path);
            Properties properties = new Properties();
            FileInputStream input = new FileInputStream(file);
            properties.load(input);

            userName = properties.getProperty("super_admin_username");
            password = properties.getProperty("super_admin_password");
            serverHostname = properties.getProperty("server_hostname");
            jmsPort = properties.getProperty("jms_port");

            while (true) {
                javax.jms.TopicSubscriber subscriber = subscribe();
                receive(subscriber);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static javax.jms.TopicSubscriber subscribe() throws NamingException, JMSException {

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));
        InitialContext ctx = new InitialContext(properties);
        // Lookup connection factory
        TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup(CF_NAME);
        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        topicSession = topicConnection.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);

        Topic topic = topicSession.createTopic(topicName);
        topicSubscriber = topicSession.createSubscriber(topic);
        return topicSubscriber;
    }

    public static void receive(javax.jms.TopicSubscriber topicSubscriber) throws NamingException, JMSException {
        Message message = topicSubscriber.receive();
        System.out.println("Got message from topic subscriber = " + message);
    }

    private static String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(serverHostname).append(":").append(jmsPort).append("'")
                .toString();
    }
}
