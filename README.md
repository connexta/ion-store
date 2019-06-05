# Multi-Int Store

## Prerequisites
* Java 11
* Docker

## Building
```
./gradlew build
```
Because the Docker images are built automatically, a docker daemon must be available.

## Running via docker-compose
There is a docker-compose environment included in this repository.
This will spin up each of the services that make up the multi-int-store as well as any 3rd party services needed by the multi-int-store.

A `cdr` network is needed to run via docker-compose. First, check if the `cdr` network is already created:
```
docker network ls
```

If it has not yet been created, execute:
```
docker network create --driver=overlay --attachable cdr
```

To check that it has been created successfully, execute:
```
docker network ls
```

To start the full environment, execute:
```
docker-compose up -d
```

To check the status of the environment, execute:
```
docker-compose ps
```

To stream the logs to the console, execute:
```
docker-compose logs -f
```

To bring down the services and clean up, execute:
```
docker-compose down
```

### Accessing

The compose environment is configured to expose each of the services to the host OS on a different port. To check the ports examine the output of `docker-compose ps`.

### Tests
Tests are run automatically with `./gradlew build`. Even if the tests fail, the artifacts are built and can be run.

To run tests with quiet logs, add `-Pquiet`. This should not be used with parallel builds.

To run a single test suite, execute:
```
./gradlew module:test --test fullClassName
```

#### Integration Tests
The integration tests require that Docker is running.

To skip integration tests, add `-PskipITests`.

## Running
1. Boot up an [Apache Cassandra](https://cassandra.apache.org/) instance using the default hostname and ports: `localhost:9042`.

2. To start the services, execute:
	```
	./gradlew run
	```

## Build Checks
### OWASP
```
./gradlew dependencyCheckAnalyze --info
```

### Style
```
./gradlew spotlessApply
```

For more information about spotless checks see [here](https://github.com/diffplug/spotless/tree/master/plugin-gradle#custom-rules).
