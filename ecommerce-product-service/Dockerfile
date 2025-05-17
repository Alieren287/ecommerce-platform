FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copy maven wrapper and pom.xml files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Copy parent pom if exists
COPY ../pom.xml ../pom.xml

# Make the mvnw script executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Create a minimal runtime image
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.alier.ecommerceproductservice.EcommerceProductServiceApplication"] 