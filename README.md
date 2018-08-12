# MoneyPal [![Build Status](https://travis-ci.org/spolnik/payment-service.svg?branch=master)](https://travis-ci.org/spolnik/payment-service) [![codecov](https://codecov.io/gh/spolnik/payment-service/branch/master/graph/badge.svg)](https://codecov.io/gh/spolnik/payment-service) 

Money transfer microservice example

## Assumptions

Although the quality of code is production ready, in overall there is few simplifications made for making implementation doable
and fairly simple:

- service is assumed to be used in secured network, all operations are assumed to be secured by Edge/Auth Service
- service keeps own representation of data - payment and account. It's highly probable that especially account is 
represented and managed in other place - and that's okay. In here we create just slice of account domain which is
needed for service functionality to work in isolated way 

## Technologies

- Spark Java
- Java 8
- Xodus DB

## Testing

- rest-assured
- assertj
- junit
- Spock (E2E)

## Infrastructure

- Gradle
- Travis CI
- Codecov
- Heroku

## Run locally

#### Gradle Wrapper

- run `./gradlew run`
- then you can open service at http://localhost:4000

#### Runnable Jar

- run `./gradlew stage` to build executable jar
- run `java -jar build/libs/payment-service-*-all.jar`

> Note: Swagger UI is configured by default to run against heroku instance, to change - before building executable 
jar modify host in [swagger-config.yaml](https://github.com/spolnik/payment-service/blob/master/src/main/resources/public/swagger-config.yaml#L11)