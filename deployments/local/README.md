# Local Minio Deployment

## Description
This is an example local deployment of the Multi Int Store that also starts up a local Minio server on
port 9000, creates an ingest-quarantine bucket, and uses that instead of an Amazon S3 store. It also
includes a deploy.sh file that attempts to create the docker network needed.

## Prerequisites
* Java 11
* Docker daemon
* Linux/Mac

## How to Run
From this directory, run the deploy script
```
./deploy.sh
```

## Minio
The Minio web client is exposed on port 9000 with the default login information
These Minio keys are in the text files `minio_access.notsec` and `minio_secret.notsec`

## Additional Information
Any additional information about the services started can be found in the README.md 
at the root of the project.
