FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
RUN apk add --no-cache maven
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY college-erp-service/pom.xml college-erp-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY notification-service/src notification-service/src
RUN mvn -pl notification-service -am clean package -DskipTests -q
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S erp && adduser -S erp -G erp
COPY --from=builder /app/notification-service/target/*.jar app.jar
RUN chown erp:erp app.jar
USER erp
EXPOSE 8082
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
