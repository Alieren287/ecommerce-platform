# Configuring Logback for MongoDB and File Storage

SLF4J is a facade (abstraction) for various logging frameworks. In Spring Boot, the default implementation is Logback.
Here's how to configure Logback to store logs in both MongoDB and files.

## 1. File-based Logging (Already Configured)

Your application already has file-based logging configured in `logback-spring.xml`:

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/application.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/application-%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId}] %logger{36} - %msg%n</pattern>
        <charset>utf8</charset>
    </encoder>
</appender>
```

This configuration:

- Stores logs in `logs/application.log`
- Rotates logs daily with pattern `logs/application-yyyy-MM-dd.log`
- Keeps 30 days of history
- Includes timestamp, thread, log level, requestId, logger name, and message

## 2. MongoDB Logging Configuration

To store logs in MongoDB, add these dependencies to your pom.xml:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>ch.qos.logback.contrib</groupId>
    <artifactId>logback-mongodb-core</artifactId>
    <version>0.1.5</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback.contrib</groupId>
    <artifactId>logback-jackson</artifactId>
    <version>0.1.5</version>
</dependency>
```

Then update your `logback-spring.xml` to include MongoDB appender:

```xml
<appender name="MONGODB" class="ch.qos.logback.contrib.mongodb.MongoDBAppender">
    <connectionUri>mongodb://username:password@localhost:27017/logs</connectionUri>
    <collectionName>application_logs</collectionName>
    <databaseName>logs</databaseName>
    <includeCallerData>true</includeCallerData>
    
    <!-- Include fields in the MongoDB document -->
    <property>
        <name>requestId</name>
        <value>%X{requestId}</value>
    </property>
    <property>
        <name>traceId</name>
        <value>%X{traceId}</value>
    </property>
    
    <!-- Configure JSON format -->
    <jsonFormatter class="ch.qos.logback.contrib.jackson.JacksonJsonFormatter">
        <prettyPrint>false</prettyPrint>
    </jsonFormatter>
</appender>

<!-- Add it to your root logger -->
<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
    <appender-ref ref="MONGODB"/>
</root>
```

## 3. Configuration in application.properties/yaml

You can also configure MongoDB connection details in your application.properties or application.yml:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://username:password@localhost:27017/logs
      database: logs
```

Then reference it in your logback-spring.xml:

```xml
<springProperty scope="context" name="mongodb.uri" source="spring.data.mongodb.uri"/>
<springProperty scope="context" name="mongodb.database" source="spring.data.mongodb.database"/>

<appender name="MONGODB" class="ch.qos.logback.contrib.mongodb.MongoDBAppender">
    <connectionUri>${mongodb.uri}</connectionUri>
    <databaseName>${mongodb.database}</databaseName>
    <collectionName>application_logs</collectionName>
    <!-- other configuration as above -->
</appender>
```

## 4. Benefits of MongoDB for Logs

Storing logs in MongoDB provides several benefits:

1. **Structured Data**: Logs are stored as documents, making them easy to query
2. **Rich Queries**: Search by requestId, traceId, timestamp, log level, etc.
3. **Indexing**: Create indexes for fast queries on common fields
4. **Aggregation**: Run aggregation pipelines for log analysis
5. **Scalability**: MongoDB can handle large volumes of logs
6. **Retention Policies**: Use TTL indexes to automatically expire old logs

## 5. Log Format

Each log entry in MongoDB will look something like:

```json
{
  "timestamp": ISODate("2023-07-31T14:52:10.123Z"),
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.alier.ecommercewebcore.rest.controller.ProductController",
  "message": "Product retrieved successfully",
  "requestId": "req-abc123",
  "traceId": "gen-xyz789",
  "callerData": {
    "fileName": "ProductController.java",
    "lineNumber": 42,
    "methodName": "getProduct"
  }
}
```

## 6. Accessing Logs

You can access MongoDB logs using:

1. **MongoDB Compass**: GUI for exploring and querying logs
2. **MongoDB CLI**: Run queries from the command line
3. **Custom Admin Portal**: Create a simple admin interface to view logs
4. **Grafana**: Connect Grafana to MongoDB for dashboards and visualization

Example MongoDB query to find all logs for a specific requestId:

```javascript
db.application_logs.find({requestId: "req-abc123"})
```

## 7. Advanced Tips

1. **Index Creation**: Create indexes for fields you frequently query:
   ```javascript
   db.application_logs.createIndex({ timestamp: 1 })
   db.application_logs.createIndex({ requestId: 1 })
   db.application_logs.createIndex({ level: 1 })
   ```

2. **TTL Index**: Automatically delete logs older than 30 days:
   ```javascript
   db.application_logs.createIndex({ timestamp: 1 }, { expireAfterSeconds: 2592000 })
   ```

3. **Separate Databases**: Use separate databases for development, testing, and production logs. 