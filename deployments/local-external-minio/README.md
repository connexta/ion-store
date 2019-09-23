# Local External Minio Deployment

## Description
This is an example local deployment of the Store Service that connects to a local Minio server and
uses that for the S3 store. It also includes a deploy.sh script that will attempt to create the
docker network needed.

## Prerequisites
* Java 11
* Docker daemon
* Linux/Mac
* Minio at http://localhost:9000 with a "ingest-quarantine" bucket and login information defined in
`secrets/minio_access.notsec` and `secrets/minio_secret.notsec`.

## How to Run
From this directory:
```bash
./deploy.sh
```
The script should provide appropriate error messages to resolve any issues that may occur while
setting up the docker network or deploying the images.

## How to Stop
From this directory:
```bash
./deploy.sh clean
```

## Additional Information
Any additional information about the services started can be found in the README.md at the root of
the project.
