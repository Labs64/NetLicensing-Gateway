FROM maven:3.6.2-jdk-8-slim AS builder
WORKDIR /opt/gateway/
COPY ./ /opt/gateway/ 
RUN mvn package

# tomcat:8.0 image from docker hub.
FROM tomcat:8.5
# COPY path-to-application-war path-to-webapps-in-docker-tomcat
COPY --from=builder /opt/gateway/target/gateway.war /usr/local/tomcat/webapps/

EXPOSE 8080/tcp