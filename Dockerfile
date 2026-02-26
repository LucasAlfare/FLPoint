FROM gradle:8.5-alpine AS build
WORKDIR /app
COPY . .
RUN gradle assemble --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 7171

COPY --from=build /app/server/build/libs/server.jar /app/server.jar

ENTRYPOINT ["java", "-jar", "/app/server.jar"]