#!/bin/sh
# This script runs inside the localstack container
echo "Creating S3 buckets..."
set -x
awslocal s3 mb s3://irm
awslocal s3 mb s3://metacard-quarantine
awslocal s3 mb s3://ingest-quarantine
set +x
echo "Done creating S3 buckets."