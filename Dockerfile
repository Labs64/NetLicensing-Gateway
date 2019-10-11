# tomcat:8.0 image from docker hub.
FROM tomcat:8.0


# COPY path-to-application-war path-to-webapps-in-docker-tomcat
COPY ./target/gateway.war /usr/local/tomcat/webapps/

VOLUMES:
	- ${PWD}/conf/tomcat-users.xml:/usr/local/tomcat/conf/tomcat-users.xml

EXPOSE 8080/tcp