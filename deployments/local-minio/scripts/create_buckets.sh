#!/bin/bash
set -x
awslocal mb s3 s3://irm
awslocal mb s3 s3://metacard-quarantine
awslocal mb s3 s3://ingest-quarantine
set +x