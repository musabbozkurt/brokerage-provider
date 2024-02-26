FROM openjdk:21-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} brokerage-provider.jar
ENTRYPOINT ["java","-jar","/brokerage-provider.jar"]
