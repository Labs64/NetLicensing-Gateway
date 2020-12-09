<img src="https://repository-images.githubusercontent.com/73901823/f634b580-38ce-11eb-8e5f-71b6f1e79d3b">

# Labs64 NetLicensing / eCommerce Gateway

[![NetLicensing Gateway - CI](https://github.com/Labs64/NetLicensing-Gateway/workflows/NetLicensing%20Gateway%20-%20CI/badge.svg)](https://github.com/Labs64/NetLicensing-Gateway/actions?query=workflow%3A%22NetLicensing+Gateway+-+CI%22)
[![NetLicensing Gateway Docker image](https://img.shields.io/badge/Docker-labs64%2Fnetlicensing--gateway-0db7ed.svg)](https://hub.docker.com/r/labs64/netlicensing-gateway)
[![NetLicensing](https://img.shields.io/badge/NetLicensing-IO-E14817.svg?logo=NetLicensing)](https://netlicensing.io)
[![📖 Documentation](https://img.shields.io/badge/📖%20Documentation-Wiki-AB6543.svg)](https://netlicensing.io/wiki/)

Use any eCommerce platform, such as *FastSpring, MyCommerce, PrestaShop, SendOwl,* and many others as a license acquisition frontend.
After a successful transaction, all needed licensing configuration (Customers, Licenses and all related data) will be made available in the NetLicensing and can be used for later entitlements validation in NetLicensing.

## License Acquisition Flow
![NetLicensing / Gateway Integration How-To](https://raw.githubusercontent.com/wiki/Labs64/NetLicensing-Gateway/images/00_external-ecommerce-flow.png)

## How to use this image

There are various ways available on how to enable NetLicensing Gateway in your licensing flow:

- Centrally hosted instance available at [gateway.netlicensing.io](https://gateway.netlicensing.io/monitoring)
- Your individual instance from Docker image (check [Docker](#docker) and [Docker Compose](#docker-compose) instructions below)

### Docker

#### Pull official NetLicensing Gateway image

```
$ docker pull labs64/netlicensing-gateway
```

#### Start container

```
$ docker container run -d -it --publish 8080:8080 --name netlicensing-gateway labs64/netlicensing-gateway
```

### Docker Compose

Additionally to the main NetLicensing Gateway instance, Docker Compose  includes various helper tools needed for local testing and eCommerce connectors development:

- [Ngrok](https://ngrok.com) - expose local NetLicensing Gateway endpoints as public URLs, to allow eCommerce fulfilment webhook tests.

#### Clone repository

```
$ git clone https://github.com/Labs64/NetLicensing-Gateway.git
```

#### Start containers
```
$ docker-compose up -d
```

#### Stop containers

```
$ docker-compose down
```

### Test configuration

#### Sanity check

Verify container configuration by opening monitoring endpoint at [http://localhost:8080/gateway/monitoring](http://localhost:8080/gateway/monitoring)

#### Connectors tests

Refer to the eCommerce connectors [setup and configuration guides](https://github.com/Labs64/NetLicensing-Gateway/wiki).

## Contributors

New connectors' integrations as community implementation are highly appreciated and welcome.
Please refer to the [contributing instructions](CONTRIBUTING.md).

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/Labs64/NetLicensing-Gateway/issues).

## License

NetLicensing Gateway is open-source software licensed under the [Apache 2.0](LICENSE) license.

## Links

- [Labs64 @ Docker Hub](https://hub.docker.com/u/labs64)
- NetLicensing Gateway [Docker image](https://hub.docker.com/r/labs64/netlicensing-gateway)
- NetLicensing [Integrations](https://netlicensing.io/wiki/integrations)
- NetLicensing [RESTful API](https://netlicensing.io/wiki/restful-api)
