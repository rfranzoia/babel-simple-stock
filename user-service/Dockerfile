FROM maven:3.8.3-openjdk-17 AS build
RUN mkdir -p /workspace
COPY temp/src /workspace/common-service/src
COPY temp/pom.xml /workspace/common-service
COPY user-service/src /workspace/app/user-service/src
COPY src /workspace/app/src
COPY user-service/pom.xml /workspace/app/user-service
COPY pom.xml /workspace/app
WORKDIR /workspace/app
RUN mvn clean install -DskipTests

FROM openjdk:17-ea-slim-buster
COPY --from=build /workspace/app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
