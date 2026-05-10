#!/usr/bin/env bash
set -euo pipefail

# create S3 bucket for local development using awslocal (bundled with LocalStack)
BUCKET=${S3_BUCKET:-seshop-media}
REGION=${S3_REGION:-us-east-1}

echo "Creating S3 bucket '${BUCKET}' in region '${REGION}' if it does not exist..."

# For us-east-1 do not pass LocationConstraint
if [ "${REGION}" = "us-east-1" ]; then
  awslocal s3api create-bucket --bucket "${BUCKET}" || true
else
  awslocal s3api create-bucket --bucket "${BUCKET}" --create-bucket-configuration LocationConstraint=${REGION} || true
fi

echo "Bucket ensured: ${BUCKET}"
