#
# Build stage
#
FROM maven:3.9.7-eclipse-temurin-17-alpine AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean install

#
# Package stage
#
FROM eclipse-temurin:21
COPY --from=build /home/app/target/classes/commands/ /usr/local/lib/classes/commands
COPY --from=build /home/app/target/cml-1.jar /usr/local/lib/cml-1.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/cml-1.jar"]