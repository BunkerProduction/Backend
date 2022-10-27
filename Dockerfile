FROM openjdk:17
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/libs/*.jar /app/ktor-bunker-sample.jar
ENTRYPOINT ["java","-jar","/app/ktor-bunker-sample.jar"]