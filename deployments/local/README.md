# Local Deployment

## Description
This is an example local deployment of the Store Service that also starts up a Localstack Docker image that provides
an implementation of the S3 API on port 4572. It also creates 3 S3 buckets: ingest-quarantine, metacard-quarantine,
and irm. A basic dashboard UI is available on port 8055 and a JSON record of all requests to S3 will be stored in
<project-root>/.localstack/data/s3_api_calls.json .


## Prerequisites
* Java 11
* Docker daemon
* Linux/Mac

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
Any additional information about the services started can be found in the README.md at the root of the project.
