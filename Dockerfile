#https://springframework.guru/docker-hub-for-spring-boot/
FROM adoptopenjdk/openjdk17:alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
#ARG JAR_FILE=target/*.jar
COPY target/todos.jar todos.jar
ENTRYPOINT ["java","-jar","/todos.jar"]
EXPOSE 80