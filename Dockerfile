#
# Build stage
#
FROM maven:3.8.4-eclipse-temurin-17-alpine AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean install

#
# Package stage
#
FROM eclipse-temurin:17
COPY --from=build /home/app/target/classes/commands/ /usr/local/lib/classes/commands
COPY --from=build /home/app/target/cml-1.jar /usr/local/lib/cml-1.jar
HEALTHCHECK --interval=5s --timeout=5s CMD ["/usr/local/lib/cml-1.jar", "--healthcheck"]
ENTRYPOINT ["java","-jar","/usr/local/lib/cml-1.jar"]