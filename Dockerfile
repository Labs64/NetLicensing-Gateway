# package project
FROM maven:3.6.2-jdk-8-slim AS builder
WORKDIR /opt/gateway/
COPY ./ /opt/gateway/
RUN mvn clean package

# start tomcat
FROM tomcat:8.5
COPY --from=builder /opt/gateway/target/gateway.war /usr/local/tomcat/webapps/

EXPOSE 8080/tcp
