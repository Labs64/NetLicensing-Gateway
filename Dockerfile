# package project
FROM maven:3-jdk-11-slim AS builder
WORKDIR /opt/gateway/
COPY ./ /opt/gateway/
RUN mvn clean package

# start tomcat
FROM tomcat:9.0-jdk11
COPY --from=builder /opt/gateway/target/gateway.war /usr/local/tomcat/webapps/

EXPOSE 8080/tcp
