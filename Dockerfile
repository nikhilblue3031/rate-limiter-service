FROM gradle:8.5-jdk17 as builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle build --no-daemon --refresh-dependencies

FROM openjdk:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]