FROM amazoncorretto:17
COPY target/sic-api-Athena.jar sic-api-Athena.jar
ADD newrelic newrelic
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/newrelic/newrelic.jar", "-jar", "sic-api-Athena.jar"]
