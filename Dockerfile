# Dockerfile
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

RUN chmod +x ./mvnw
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:17-jdk

WORKDIR /app

# non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
