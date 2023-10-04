FROM maven:3.8.3-openjdk-17 AS build
RUN mkdir -p /workspace
COPY temp/src /workspace/common-service/src
COPY temp/pom.xml /workspace/common-service
COPY order-stockmovement-service/src /workspace/app/order-stockmovement-service/src
COPY src /workspace/app/src
COPY order-stockmovement-service/pom.xml /workspace/app/order-stockmovement-service
COPY pom.xml /workspace/app
WORKDIR /workspace/app
RUN mvn clean install -DskipTests

FROM openjdk:17-ea-slim-buster
COPY --from=build /workspace/app/order-stockmovement-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
