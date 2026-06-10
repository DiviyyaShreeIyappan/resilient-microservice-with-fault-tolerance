# Referred from: https://gallery.ecr.aws/docker/library/eclipse-temurin
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY backendservice/target/backendservice-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
