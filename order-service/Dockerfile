FROM maven:3.8.3-openjdk-17 AS build
RUN mkdir -p /workspace
COPY temp/src /workspace/common-service/src
COPY temp/pom.xml /workspace/common-service
COPY order-service/src /workspace/app/order-service/src
COPY src /workspace/app/src
COPY order-service/pom.xml /workspace/app/order-service
COPY pom.xml /workspace/app
WORKDIR /workspace/app
RUN mvn clean install -DskipTests

FROM openjdk:17-ea-slim-buster
COPY --from=build /workspace/app/order-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
