FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --create-home shiftcounter
WORKDIR /app
ARG JAR_FILE=target/shiftcounter-*.jar
COPY ${JAR_FILE} app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
