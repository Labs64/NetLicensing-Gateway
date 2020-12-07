<img src="https://netlicensing.io/img/netlicensing-stage-twitter.jpg">

# Labs64 NetLicensing / eCommerce Gateway

[![Build Status](https://travis-ci.org/Labs64/NetLicensing-Gateway.svg?branch=master)](https://travis-ci.org/Labs64/NetLicensing-Gateway)

[Labs64 NetLicensing](https://netlicensing.io) is a first-class solution in the Licensing as a Service (LaaS) sector. Based on open standards, it provides a cost effective, integrated and scalable platform for software vendors and developers who want to concentrate on their product’s core functionality instead of spending resources on developing an own license management software.

## License Acquisition Flow
![NetLicensing / Gateway Integration How-To](https://raw.githubusercontent.com/wiki/Labs64/NetLicensing-Gateway/images/00_external-ecommerce-flow.png)

## Full Documentation

See the [Wiki](https://github.com/Labs64/NetLicensing-Gateway/wiki/) for full documentation, examples, operational details and other information.

See the NetLicensing [RESTful API](https://netlicensing.io/wiki/restful-api) for the service API.

## Docker Build and Deployment

Prerequisites:
* install [docker](https://docs.docker.com/install/) on you host

To build the docker image execute:
```
$ docker image build -t labs64/gateway ./
```
Where `labs64` and `gateway` are _company_name_ and _app_image_name_.

### Use standalone container
```
$ docker container run -it --publish 8081:8080 labs64/gateway
```
Application can be accessed at [localhost:8081](http://localhost:8081)

If needed, to access the admin console. Run docker container with the following command.

Note: Tomcat user configuration is located in ./dockerfiles/conf/tomcat-users.xml

```
$ docker container run -it -v $(pwd)/dockerfiles/conf/tomcat-users.xml:/usr/local/tomcat/conf/tomcat-users.xml --publish 8081:8080 labs64/gateway
```

### Use docker-compose (Tomcat + ngrok)

Start environment:
```
$ docker-compose up -d
```

The ngrok monitoring endpoint can be accessed at [127.0.0.1:4040](http://127.0.0.1:4040)

Stop environment:
```
$ docker-compose down
```

### Sanity checks

To verify whether your local development environment is up and running open following URLs in your browser:

* localhost: http://localhost:8081/gateway/monitoring
* ngrok: http://ngrok-subdomain.ngrok.io/gateway/monitoring (Note: `ngrok-subdomain` might change on every environment start)

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/Labs64/NetLicensing-Gateway/issues).
