FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app

# Copy the parent POM
COPY pom.xml /app/pom.xml

# Copy the core module
COPY ecommerce-core /app/ecommerce-core
COPY ecommerce-web-core /app/ecommerce-web-core

# Copy product service module
COPY ecommerce-product-service/pom.xml /app/ecommerce-product-service/pom.xml
COPY ecommerce-product-service/src /app/ecommerce-product-service/src

# Build the project skipping tests
RUN mvn clean package -DskipTests -pl ecommerce-product-service -am

FROM openjdk:17-slim
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/ecommerce-product-service/target/ecommerce-product-service-*.jar /app/app.jar

# Create volume for logs
VOLUME /app/logs

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"] 