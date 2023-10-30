# TopicSubscriber

This client can be used to subscribe into JMS Topic of WSO2 API Manager and consume the messages it retrieves. This will be helpful to troubleshoot some scenarioes like JMS events are not gtting consumed by the Gateway profile from Traffic Manager node. 

## How it works

We should provide the location of config.properties file as a parameter which includes super admin's username, password and hostname, jms port combinations.

### Option-1

1. Clone the project into local environment.
2. Run the main class "TopicSubscriber.java" using an IDE by providing the config.properities file as an argument.
3. Navigate into the Publisher portal and create and deploy a new REST API. We can see the events elated to thes operation will be getting consumed by this java client.

### Option-2

1. Clone the project into local environment.
2. Build using "mvn clean install" command.
3. Run the built .jar file by executing below command. We should provide the config.properties file as a command line argument here. <br />
   **java -jar TopicSubscriber-1.0-SNAPSHOT-jar-with-dependencies.jar config.properties > output.txt 2>&1**
4. Events will be consumed and persisted into a file called output.txt in the same directory.
