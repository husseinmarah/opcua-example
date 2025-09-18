# opcua-example
Simple example of the [OPC UA](https://opcfoundation.org/about/opc-technologies/opc-ua/) implementation using [Eclipse Milo](https://github.com/eclipse-milo/milo) Library

#### OPC UA Client SDK
```xml
<dependency>
    <groupId>org.eclipse.milo</groupId>
    <artifactId>sdk-client</artifactId>
    <version>0.6.9</version>
</dependency>
```
#### OPC UA Server SDK
```xml
<dependency>
    <groupId>org.eclipse.milo</groupId>
    <artifactId>sdk-server</artifactId>
    <version>0.6.9</version>
</dependency>
```

## Running the example

#### Excecute the project

`$ mvn clean install`

#### Run the server

`$ mvn -f pom.xml exec:java`
or
`$ mvn -f pom.xml exec:java@ServerMain`

#### Run the client

`$ mvn -f pom.xml exec:java`
or
`$ mvn -f .\pom.xml exec:java@ClientMain`

#### Run the subsctiption client

`$ mvn -f pom.xml exec:java`
or
`$ mvn -f .\pom.xml exec:java@SubscriptionClient`
