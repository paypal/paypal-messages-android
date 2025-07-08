# Central Portal API Publishing Guide

This document describes how to publish snapshots to Maven Central using the Central Portal API workflow.

## Overview

As of July 2025, the OSSRH service has been sunset, and publishing to Maven Central requires using the Central Portal. This repository includes a GitHub Actions workflow that handles the entire publishing process using the Central Portal API.

## Prerequisites

1. **Central Portal Account**: You need a Sonatype Central Portal account with the necessary permissions for the `com.paypal.messages` namespace.

2. **Central Portal API Token**: Generate an API token from your Central Portal account. This should be stored as a GitHub secret named `CENTRAL_PORTAL_TOKEN`.

3. **Signing Keys**: You need GPG signing keys for signing the artifacts. These should be stored as GitHub secrets:
   - `SIGNING_KEY_FILE`: The GPG private key file encoded as base64
   - `SIGNING_KEY_ID`: The key ID
   - `SIGNING_KEY_PASSWORD`: The key password

## Publishing Process

### Using the GitHub Actions Workflow

1. Go to the "Actions" tab in your GitHub repository.

2. Select the "Central Portal Snapshot Release" workflow.

3. Click on "Run workflow" and provide the following inputs:
   - **Publishing type**: Choose between:
     - `AUTOMATIC`: The bundle will be automatically published after validation.
     - `USER_MANAGED`: Manual publishing is required after validation.
   - **Snapshot version** (optional): Specify a custom version number. If not provided, a timestamp-based version will be generated.

4. Click "Run workflow" to start the process.

### Workflow Steps

1. **Build and Sign**: The workflow builds the project and signs the artifacts.

2. **Create Bundle**: A Maven-compatible bundle is created with all required artifacts.

3. **Upload to Central Portal**: The bundle is uploaded to the Central Portal API.

4. **Monitor Status**: The workflow monitors the deployment status until it's either published or failed.

## Manual Publishing

If you selected `USER_MANAGED` as the publishing type, you'll need to manually publish the bundle:

1. Go to the Central Portal website.

2. Navigate to your deployments list.

3. Find the deployment by its name (e.g., `paypal-messages-1.0.4-SNAPSHOT`).

4. Click "Publish" to publish the validated bundle.

## Troubleshooting

- **Authentication Issues**: Ensure your Central Portal token is correct and has the necessary permissions.

- **Bundle Structure Issues**: Check the logs for any issues with the bundle creation. The bundle must follow Maven Central requirements.

- **Failed Validation**: If the bundle fails validation, the logs will contain the reasons. Fix the issues and try again.

## Local Testing

You can test the bundle creation locally by running:

```bash
./.github/scripts/create-maven-bundle.sh \
  --version "1.0.4-SNAPSHOT" \
  --group-id "com.paypal.messages" \
  --artifact-id "paypal-messages" \
  --output-dir "bundle" \
  --output-zip "bundle.zip"
```

This will create a bundle similar to what the workflow would create.