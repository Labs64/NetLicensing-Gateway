Contributions are welcome, and are accepted via pull requests. Please review these guidelines before submitting any pull requests.

## General Rules

* As much as possible, try to follow the existing format of markdown and code.
* Don't forget to document your connector before submitting pull requests.
* Make sure that 100% of your code is covered by tests.

## Local Build and Deployment

Prerequisites:

- install [Docker](https://docs.docker.com/install/) on you host machine

#### Clone repository

```
$ git clone https://github.com/Labs64/NetLicensing-Gateway.git
```

#### Build local docker image

```
$ docker image build -t labs64/netlicensing-gateway .
```

... and start your local container using Docker or Docker Compose

### Docker

#### Start standalone container (Docker)

```
$ docker container run -d -it --publish 8080:8080 --name netlicensing-gateway labs64/netlicensing-gateway
```

Application can be accessed at [localhost:8080](http://localhost:8080)

To enable access to Tomcat admin console, run Docker container with the following command:

```
$ docker container run -d -it -v $(pwd)/dockerfiles/conf/tomcat-users.xml:/usr/local/tomcat/conf/tomcat-users.xml --publish 8080:8080 --name netlicensing-gateway labs64/netlicensing-gateway
```

### Docker Compose (Tomcat + ngrok)

Additionally to the main NetLicensing Gateway instance, Docker Compose  includes various helper tools needed for local testing and eCommerce connectors development:

- [Ngrok](https://ngrok.com) - expose local NetLicensing Gateway endpoints as public URLs, to allow eCommerce fulfilment webhook tests.

#### Start environment

```
$ docker-compose up -d
```

The ngrok monitoring endpoint can be accessed at [localhost:4040](http://localhost:4040)

#### Stop environment

```
$ docker-compose down
```

### Sanity checks

To verify whether your local development environment is up and running open following URLs in your browser:

- **gateway:** http://localhost:8080/gateway/monitoring
- **ngrok:** https://[ngrok-subdomain].ngrok.io/gateway/monitoring (Note: `ngrok-subdomain` will be different upon every environment start)

### Database connect

*NOTE:* Database available in the `Development` mode only (see POM profile and web.xml configuration).

You can access H2 web console with your web browser at [localhost:8082](http://localhost:8082) with the following settings:

- **JDBC URL:** `jdbc:h2:mem:dataSource`
- **User Name:** `sa`
- **Password:** `<EMPTY>`

... or access port 9092 with an external SQL client.
