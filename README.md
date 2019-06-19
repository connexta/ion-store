# Multi-Int Store

## Prerequisites
* Java 11
* Docker daemon

## Working with IntelliJ or Eclipse
This repository uses [Lombok](https://projectlombok.org/), which requires additional configurations and plugins to work in IntelliJ / Eclipse.
Follow the instructions [here](https://www.baeldung.com/lombok-ide) to set up your IDE.

## Building
```bash
./gradlew build
```

### Build Checks
#### OWASP
```bash
./gradlew dependencyCheckAnalyze --info
```
The report for each project can be found at build/reports/dependency-check-report.html.

#### Style
The build can fail because the static analysis tool, Spotless, detects an issue. To correct most Spotless issues:
```bash
./gradlew spotlessApply
```

For more information about spotless checks see
[here](https://github.com/diffplug/spotless/tree/master/plugin-gradle#custom-rules).

#### Tests
* Tests are run automatically with `./gradlew build`.
* To skip all tests, add `-x test`.
* Even if the tests fail, the artifacts are built and can be run.
* To run tests with quiet logs, add `-Pquiet`. This should not be used with parallel builds.
* To run a single test suite:
    ```bash
    ./gradlew module:test --test fullClassName
    ```

##### Integration Tests
* The integration tests require a Docker daemon.
* To skip integration tests, add `-PskipITests`.

## Running

### Run Locally
Start the service using one of the following:
* [`docker-compose`](#start-the-service-locally-via-docker-compose)
* [`docker stack`](#start-the-service-locally-via-docker-stack)

To determine the ports assigned to the services:
```bash
docker-compose ps
```

#### Start the Service Locally via `docker-compose`
1. A Docker network named `cdr` is needed to run via docker-compose.

    1. Determine if the network already exists:
        ```bash
        docker network ls
        ```
        If the network exists, the output includes a reference to it:
        ```bash
        NETWORK ID          NAME                DRIVER              SCOPE
        zk0kg1knhd6g        cdr                 overlay             swarm
        ```
    2. If the network has not been created:
        ```bash
        docker network create --driver=overlay --attachable cdr
        ```
2. Start the Docker service:
    ```bash
    docker-compose up -d
    ```

##### Helpful `docker-compose` Commands
* To stop the Docker service:
    ```bash
    docker-compose down
    ```
* To stream the logs to the console:
    ```bash
    docker-compose logs
    ```
* To stream the logs to the console for a specific service:
    ```bash
    docker-compose logs -f <service_name>
    ```

#### Start the Service Locally via `docker stack`
```bash
docker stack deploy -c docker-compose.yml cdr
```

##### Helpful `docker stack` Commands
* To stop the Docker service:
    ```bash
    docker stack rm cdr
    ```
* To check the status of all services in the stack:
    ```bash
    docker stack services cdr
    ```
* To stream the logs to the console:
    ```bash
    docker service logs
    ```
* To stream the logs to the console for a specific service:
    ```bash
    docker service logs -f <service_name>
    ```

### Run on a Docker Swarm
1. Tag each image.

    To successfully push an image to a registry, the IP or hostname of the registry, along with its port number must be
    added to the name of the image. This is accomplished by the `docker tag` command which creates and alias to a Docker
    image.

    To see the images in your local image cache:
    ```bash
    docker image ls
    ```

    Look in the list for the images to be deployed:
    ```bash
    REPOSITORY                                     TAG                 IMAGE ID            CREATED             SIZE
    cnxta/cdr-ingest                               0.1.0-SNAPSHOT      4ca707d86ddb        2 hours ago         290MB
    cnxta/cdr-multi-int-store                      0.1.0-SNAPSHOT      39b44248f9c1        19 hours ago        308MB
    cnxta/cdr-search                               0.1.0-SNAPSHOT      4c29a3d8b5fa        25 hours ago        290MB
    ```

    For each image, use `docker tag SOURCE TARGET` to create an alias with the address of the target registry. For
    example, if the address of the target registry is `<docker_registry>`:
    ```bash
    docker tag cnxta/cdr-ingest:0.1.0-SNAPSHOT <docker_registry>/cnxta/cdr-ingest:0.1.0-SNAPSHOT
    docker tag cnxta/cdr-multi-int-store:0.1.0-SNAPSHOT <docker_registry>/cnxta/cdr-multi-int-store:0.1.0-SNAPSHOT
    docker tag cnxta/cdr-search:0.1.0-SNAPSHOT <docker_registry>/cnxta/cdr-search:0.1.0-SNAPSHOT
    ```
2. Push each image.

    ```bash
    docker push <docker_registry>/cnxta/cdr-ingest:0.1.0-SNAPSHOT
    docker push <docker_registry>/cnxta/cdr-multi-int-store:0.1.0-SNAPSHOT
    docker push <docker_registry>/cnxta/cdr-search:0.1.0-SNAPSHOT
    ```
3. Deploy the service in the cloud.
    > **Note**: All of the commands in this section must be executed from the cloud environment, not the local
    environment.

    ```bash
    docker stack deploy -c <(REGISTRY=<docker_registry> docker-compose config) cdr-stack
    ```
    > **Note**: Replace *<docker_registry>* in the above command with the
    *ip:port* or *host:port* of the Docker registry.

    This command first sets the `REGISTRY` environment variable and then runs `docker-compose config` to substitute the
    value of the variable into the compose file. It then takes the contents of the compose file with the substituted
    text and redirects it to `stdin`.

#### Helpful Docker swarm commands
> **Note**: All of the commands in this section must be executed from the cloud environment, not the local environment.
* To monitor the service:
    ```bash
    docker stack services cdr-stack
    ```
    ```bash
    docker stack ps cdr-stack
    ```
* To stop the service:
    ```bash
    docker stack rm cdr-stack
    ```

## Configuration
Services can be configured with an external configuration file that will be applied to the docker container during
deployment. The configuration YAML files can be found under: `<PROJECT_ROOT>/configs/` and are not verison-controlled.
The properties in these files will be merged with any properties that you have configured in the service. The properties
in the external config file take precedence over config files that are built with the service.

## Using

### Ingest Service
The Ingest service is capable of storing data in an S3-compatible data store. The configuration to access S3 is found as
a list of commands under the ingest service in the `docker-compose.yml` file. Here you can change the endpoint URL, the
S3 bucket name, and the credentials the Ingest Service will use to connect to S3. The Ingest Service uses docker secrets
for the AWS Access Key and the AWS Secret Key. The key values are stored in files called `aws_s3_access.sec` and
`aws_s3_secret.sec`. These files must be in the same directory as the `docker-compose.yml` and will not be version
controlled.

Example configs/ingest_config.yml:
```yaml
aws:
  s3:
    endpointUrl: https://s3.us-west-1.amazonaws.com
    region: us-west-1
    bucket:
      quarantine: ingest-quarantine
endpointUrl:
  transform: http://localhost:1231/transform/
  ingest:
    callback: http://localhost:1232/store/
    retrieve: http://localhost:1233/retrieve/
```

Example configs/mis_config.yml:
```yaml
solr:
  host: localhost
  port: 9983
endpointUrl:
  retrieve: http://localhost:1233/retrieve/
```
