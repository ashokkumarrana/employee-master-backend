FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY common-service common-service
COPY dropdown-service dropdown-service
RUN cd common-service && chmod +x gradlew && ./gradlew jar -x test --no-daemon \
    && cd ../dropdown-service && chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
COPY --from=build /build/dropdown-service/build/libs/*-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]