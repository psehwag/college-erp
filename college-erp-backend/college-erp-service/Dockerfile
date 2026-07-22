FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
# Plain HTTP (port 80) to Ubuntu's mirror pool is unreachable/blackholed on
# some networks while HTTPS (443) works fine (apt has had native HTTPS
# transport built in since well before Jammy) — switch sources to HTTPS
# rather than fight the network path. Also fail fast + retry on any
# individual dead connection instead of hanging on the default timeout.
RUN sed -i 's|http://archive.ubuntu.com|https://archive.ubuntu.com|g; s|http://security.ubuntu.com|https://security.ubuntu.com|g' /etc/apt/sources.list \
    && echo 'Acquire::Retries "5"; Acquire::http::Timeout "10"; Acquire::https::Timeout "10";' > /etc/apt/apt.conf.d/80-retries \
    && apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*
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
# glibc base, not Alpine/musl): libgomp for OpenBLAS's OpenMP threading, and
# libgtk2.0-0 because CascadeClassifier (objdetect) transitively loads the
# opencv_highgui module even though this headless service never uses it.
# See builder stage above for why sources.list is rewritten to HTTPS.
RUN sed -i 's|http://archive.ubuntu.com|https://archive.ubuntu.com|g; s|http://security.ubuntu.com|https://security.ubuntu.com|g' /etc/apt/sources.list \
    && echo 'Acquire::Retries "5"; Acquire::http::Timeout "10"; Acquire::https::Timeout "10";' > /etc/apt/apt.conf.d/80-retries \
    && apt-get update && apt-get install -y --no-install-recommends libgomp1 libgtk2.0-0 && rm -rf /var/lib/apt/lists/* \
    && groupadd -r erp && useradd -r -g erp erp
COPY --from=builder /app/college-erp-service/target/*.jar app.jar
RUN chown erp:erp app.jar
USER erp
EXPOSE 8081
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
