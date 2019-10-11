# tomcat:8.0 image from docker hub.
FROM tomcat:8.5


# COPY path-to-application-war path-to-webapps-in-docker-tomcat
COPY ./target/gateway.war /usr/local/tomcat/webapps/

EXPOSE 8080/tcp