FROM openjdk:17-alpine
COPY target/sic-api-Athena.jar sic-api-Athena.jar
ADD newrelic /newrelic
ENTRYPOINT ["java", "-javaagent:/newrelic/newrelic.jar", "-jar", "sic-api-Athena.jar"]
