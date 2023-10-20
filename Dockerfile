FROM amazoncorretto:17-alpine-jdk
COPY target/api-Athena.jar api-Athena.jar
ADD newrelic newrelic
EXPOSE 8080
RUN apk --no-cache add msttcorefonts-installer fontconfig && update-ms-fonts && fc-cache -f
ENTRYPOINT ["java", "-javaagent:/newrelic/newrelic.jar", "-jar", "api-Athena.jar"]
