FROM eclipse-temurin:17-jdk

ARG JAR_FILE_PATH=build/libs/*.jar
COPY ${JAR_FILE_PATH} api.jar

ENTRYPOINT ["java", "-jar", "-Djava.net.preferIPv4Stack=true", "api.jar"]