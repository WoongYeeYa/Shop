FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package

FROM tomcat:9.0-jre17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /workspace/target/my-shop.war /usr/local/tomcat/webapps/my-shop.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
