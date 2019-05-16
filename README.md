# Multi-Int Store

## Building
```
./gradlew build
```

## Running
Boot up an [Apache Cassandra](https://cassandra.apache.org/) instance
using the default hostname and ports: `localhost:9042`
```
./gradlew run
```
## Running the services in Docker containers
Each module contains a Dockerfile that specifies how to create the docker image.
In the root directory there is `docker-compose.yml` file that is used to go through each module
and build the images.

To build the images:
```
docker-compose -f docker-compose.yml build
```
To start up containers based on the images:
```
docker-compose up
```
To shutdown the containers:
```
docker-compose down
```
## Build Checks
* ### OWASP check
	`./gradlew dependencyCheckAnalyze --info`

* ### Style Checks
	`./gradlew spotlessApply`

For more information about spotless checks see
[here](https://github.com/diffplug/spotless/tree/master/plugin-gradle#custom-rules).

## Running Tests
For running a single test suite.
`./gradlew module:test --test fullClassName`

Running tests with quiet logs. This should not be used with parallel builds.

`./gradlew -Pquiet test`
