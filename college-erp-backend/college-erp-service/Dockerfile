FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY college-erp-service/pom.xml college-erp-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY college-erp-service/src college-erp-service/src
RUN mvn -pl college-erp-service -am clean package -DskipTests -q
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# javacv-platform ships glibc-linked OpenCV/FFmpeg native libs (needs a real
# glibc base, not Alpine/musl) plus libgomp for OpenBLAS's OpenMP threading.
RUN apt-get update && apt-get install -y --no-install-recommends libgomp1 && rm -rf /var/lib/apt/lists/* \
    && groupadd -r erp && useradd -r -g erp erp
COPY --from=builder /app/college-erp-service/target/*.jar app.jar
RUN chown erp:erp app.jar
USER erp
EXPOSE 8081
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
