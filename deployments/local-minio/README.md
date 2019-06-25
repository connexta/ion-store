# Local Minio Deployment

## Description
This is an example local deployment of the Multi Int Store that also starts up a local Minio server on
port 9000, creates an ingest-quarantine bucket, and uses that instead of an Amazon S3 store. It also
includes a deploy.sh file that will attempt to create the docker network needed.

## Prerequisites
* Java 11
* Docker daemon
* Linux/Mac

## How to Run
From this directory, run and follow the prompts to complete deployment:
```
./deploy.sh
```
The script should provide appropriate error messages to resolve any issues that may occur while setting up
the docker network or deploying the images.

## Minio
The minio web client is exposed on port 9000 with the default login information:
```
Access Key: MINIOEXAMPLEACCESSKEY
Secret key: MINIOEXAMPLESECRETKEY
```
These keys can also be found in `secrets/minio_access.notsec` and `secrets/minio_secret.notsec`

## Additional Information
Any additional information about the services started can be found in the README.md at the root of the project.
