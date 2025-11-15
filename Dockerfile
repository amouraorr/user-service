FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /work
COPY pom.xml .
COPY user-service/pom.xml user-service/pom.xml
COPY . .
RUN mvn -B -pl user-service -am -DskipTests package

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /work/user-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]