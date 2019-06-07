# Multi-Int Store

## Prerequisites
* Java 11
* Docker

## Build Procedures


### Build
To build and test the application:

```bash
./gradlew build
```
(NOTE: Because the Docker images are built automatically, a docker daemon must be available.)

To run OWASP checks

```bash
./gradlew dependencyCheckAnalyze --info
```

The build can fail because the static analysis tool, Spotless, detects an issue. To correct
most issues, ask Spotless to apply style and formatting changes:

```bash
./gradlew spotlessApply
```
For more information about spotless checks see
[here](https://github.com/diffplug/spotless/tree/master/plugin-gradle#custom-rules).


### Test
* Tests are run automatically with `./gradlew build`.
* Even if the tests fail, the artifacts are built and can be run.
* To run tests with quiet logs, add `-Pquiet`. This should not be used with parallel builds.
* To run a single test suite, execute:

```bash
./gradlew module:test --test fullClassName
```

* Integration tests
* The integration tests require that Docker is running.
* To skip integration tests, add `-PskipITests`.

----

## Run Services Locally
* Start the services using either **Docker compose** or **Docker stack**

* For either option, examine the output of `docker-compose ps` to determine
the **ports assigned to the services**.

### Option 1: Run services using docker-compose
There is a docker-compose environment included in this repository.
This will spin up each of the services that make up the multi-int-store as well
as any 3rd party services needed by the multi-int-store.

#### Docker network
A Docker network named `cdr` is needed to run via docker-compose.
If the network has not created on the machine, create it.

Determine if the network already exists:

```bash
docker network ls
```

If the network exists, the output includes a reference to it:

```bash
NETWORK ID          NAME                DRIVER              SCOPE
zk0kg1knhd6g        cdr                 overlay             swarm
```

If it has not yet been created, execute:

```bash
docker network create --driver=overlay --attachable cdr
```

#### Start the Docker service
To start the full environment, execute:

```bash
docker-compose up -d
```

To stream the logs to the console, execute: `docker-compose logs -f`


#### Stop the Docker service
To bring down the services and clean up, execute:

```bash
docker-compose down
```

### Option 2: Run the Docker service using docker stack
To deploy the full environment onto a swarm, execute:
```bash
docker stack deploy -c docker-compose.yml cdr
```

To check the status of all services in the stack, execute:
```bash
docker stack services cdr
```

To stream the logs for a specific service, execute:
```bash
docker service logs -f <service_name>
```
----
## Run Service on Govcloud
After the applicaiton is successfully build, the resulting images can be tagged and pushed
to the Govcloud Docker registry. Once the images are in the registry, they can be deployed.

### Step 1: Tag each image
To successfully push an image to a registry, the IP or hostname of the registry, along with
its port number must be added to the name of the image. This is accomplished by the `docker tag`
command which crates and alias to a Docker image.

To see the images in your local image cache, execute:
```bash
docker iamge ls
```

Look in the list for the images to be deployed:
```
REPOSITORY                                     TAG                 IMAGE ID            CREATED             SIZE
cnxta/cdr-ingest                               0.1.0-SNAPSHOT      4ca707d86ddb        2 hours ago         290MB
cnxta/cdr-multi-int-store                      0.1.0-SNAPSHOT      39b44248f9c1        19 hours ago        308MB
cnxta/cdr-search                               0.1.0-SNAPSHOT      4c29a3d8b5fa        25 hours ago        290MB
```

For each image, use `docker tag SOURCE TARGET` to create an alias with the address of the
target registry. For example, if the address of the target registry is `160.1.120.126:5000`,
execute then the `cnxta/cdr-ingest` should be tagged:

```bash
docker tag cnxta/cdr-ingest:0.1.0-SNAPSHOT 160.1.120.126:5000/cnxta/cdr-ingest:0.1.0-SNAPSHOT
```

See also [Docker naming conventions](docker-naming-convension.md).

### Step 2: Push each image
Push each tagged image. The `docker push` will use the registry information from the repository
name to determine where to push the image.

```bash
docker push 160.1.120.126:5000/cnxta/cdr-ingest:0.1.0-SNAPSHOT
```

### Step 3: Deploy Docker service
Deploy the service in the cloud.

```bash
gc-docker stack deploy -c <(REGISTRY=160.1.120.126:5000 docker-compose config) cdr
```
(NOTE: This command first sets the REGISTRY environment variable and then runs
`docker-compose config` to subsitute the value of the variable into the compose file. It then
takes the contents of the compose file with the substituted text and redirects it to `stdin`)

### Step 4: Monitor the Docker service
Use the commands

```bash
gc-docker stack services cdr
```
and

```bash
gc-docker stack ps cdr
```

### Stop the Docker service

```bash
gc-docker stack rm cdr
```
