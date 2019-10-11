<img src="https://netlicensing.io/img/netlicensing-stage-twitter.jpg">

# Labs64 NetLicensing / eCommerce Gateway

[![Build Status](https://travis-ci.org/Labs64/NetLicensing-Gateway.svg?branch=master)](https://travis-ci.org/Labs64/NetLicensing-Gateway)

[Labs64 NetLicensing](https://netlicensing.io) is a first-class solution in the Licensing as a Service (LaaS) sector. Based on open standards, it provides a cost effective, integrated and scalable platform for software vendors and developers who want to concentrate on their product’s core functionality instead of spending resources on developing an own license management software.

## License Acquisition Flow
![NetLicensing / Gateway Integration How-To](https://raw.githubusercontent.com/wiki/Labs64/NetLicensing-Gateway/images/00_external-ecommerce-flow.png)

## Full Documentation

See the [Wiki](https://github.com/Labs64/NetLicensing-Gateway/wiki/) for full documentation, examples, operational details and other information.

See the NetLicensing [RESTful API](https://www.labs64.de/confluence/x/pwCo) for the service API.

## Docker Deployment

Prerequisites install [docker](https://docs.docker.com/install/) on you host. 

To build the docker run: 
```
> $ docker image build -t labs64/gateway ./
```
Where `labs64` and `gateway` is _company_name_ and _app_image_name_.

When the build is done. Run the container: 
```
> $ docker container run -it --publish 8081:8080 labs64/gateway
```

Now application can be accessed here : [http://localhost:8081](http://localhost:8081).

If needed, to access the admin console. Run docker container with following command.
*(Be-aware: that Tomcat user configuration is located in ./conf/tomcat-users.xml)*

```
> $ docker container run -it -v $(pwd)/conf/tomcat-users.xml:/usr/local/tomcat/conf/tomcat-users.xml --publish 8081:8080 labs64/gateway
```

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/Labs64/NetLicensing-Gateway/issues).
