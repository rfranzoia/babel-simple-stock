FROM maven:3.8.3-openjdk-17 AS build
RUN mkdir -p /workspace
COPY src /workspace/src
COPY pom.xml /workspace
WORKDIR /workspace
RUN mvn clean package -DskipTests

FROM openjdk:17-ea-slim-buster
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
