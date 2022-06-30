FROM --platform=linux/amd64 openjdk:18.0-slim-buster
ARG JAR_FILE=target/springbootemployee-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]