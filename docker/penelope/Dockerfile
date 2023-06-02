FROM eclipse-temurin:17-jdk-alpine

WORKDIR /var/penelope

COPY ./ ./

RUN ./gradlew build -x test

ENTRYPOINT [ "java", "-jar", "./build/libs/penelope-0.0.1-SNAPSHOT.jar" ]