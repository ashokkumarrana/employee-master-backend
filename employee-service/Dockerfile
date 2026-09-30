FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY common-service common-service
COPY employee-service employee-service
RUN cd common-service && chmod +x gradlew && ./gradlew jar -x test --no-daemon \
    && cd ../employee-service && chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/employee-service/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]