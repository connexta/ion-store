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
