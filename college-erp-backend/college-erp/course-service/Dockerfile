FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
# Cache dependencies
RUN apk add --no-cache maven
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S erp && adduser -S erp -G erp
COPY --from=builder /app/target/*.jar app.jar
RUN chown erp:erp app.jar
USER erp
EXPOSE 8086
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
