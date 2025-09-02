# ========== Build stage ==========
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) Copy the pom and download dependencies (caches nicely)
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# 2) Copy the source and build
COPY src ./src
RUN mvn -B -q -DskipTests package

# ========== Run stage ==========
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
