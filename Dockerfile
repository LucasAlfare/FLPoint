FROM gradle:8.5-alpine as gradle_stage
LABEL authors="Francisco Lucas"
COPY . /app
WORKDIR /app
RUN cd /app
RUN gradle clean
# gradle assemble is used to build without running tests
# also daemons are not needed because gradle will be discarded
RUN gradle assemble --no-daemon

FROM openjdk:17-alpine as jdk_stage
EXPOSE 7171
RUN mkdir /app
COPY --from=gradle_stage /app/server /app
ENTRYPOINT ["java", "-jar", "/app/build/libs/server.jar"]