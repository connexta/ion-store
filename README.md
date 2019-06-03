# Multi-Int Store

## Building
```
./gradlew build
```

### Running Tests
Tests are run automatically with `./gradlew build`. The tests require that Docker is running. Even if the tests fail, the artifacts are built and can be run.

To skip integration tests, add `-PskipITests`.

To run tests with quiet logs, add `-Pquiet`. This should not be used with parallel builds.

To run a single test suite, execute:
```
./gradlew module:test --test fullClassName
```

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
