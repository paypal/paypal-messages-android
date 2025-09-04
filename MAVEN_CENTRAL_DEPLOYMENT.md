# Maven Central Deployment Guide

This document outlines the process for deploying the PayPal Messages Android SDK to Maven Central. It addresses common issues and provides solutions to ensure successful deployment.

## Requirements for Maven Central

Maven Central has strict requirements for artifact submissions:

1. POM files must have proper XML formatting
2. Required metadata in POM:
   - Project name
   - Project description
   - Project URL
   - License information
   - SCM information
   - Developers information
3. Signature files for all artifacts (*.asc)

## Preparing and Verifying Artifacts

- Generate artifacts and POMs:
```bash
./prepare-maven-artifacts.sh
```

- Optionally verify the generated POMs meet Central requirements:
```bash
./verify_maven_central.sh
```

## Deployment Process

### Option 1: One-Step Deployment (Recommended)

Use the unified deployment script:

```bash
./deploy-to-maven-central.sh [--auto-publish]
```

- The `--auto-publish` flag will automatically publish the artifacts after validation. Without this flag, you'll need to manually publish from the Sonatype Central Portal.

### Option 2: Step-by-Step Deployment

If you prefer more control, you can run the steps individually:

1. Prepare Maven artifacts:
   ```bash
   ./prepare-maven-artifacts.sh
   ```

2. Verify Maven Central requirements (optional):
   ```bash
   ./verify_maven_central.sh
   ```

3. Deploy to Maven Central:
   ```bash
   ./deploy-to-maven-central.sh [--auto-publish]
   ```

## Common Issues

### 1. Missing Signature Files
Ensure GPG credentials are configured (`SIGNING_KEY_ID`, `SIGNING_KEY_PASSWORD`, `SIGNING_KEY_FILE`) and re-run the scripts.

### 2. Missing Metadata
Ensure your POMs are generated via `prepare-maven-artifacts.sh`, which includes all required fields (name, description, URL, license, SCM, developers).

## Deployment Steps Summary

1. Set up credentials for Sonatype Central Portal:
   ```bash
   export SONATYPE_NEXUS_USERNAME="your-username"
   export SONATYPE_NEXUS_PASSWORD="your-password"
   ```
2. Set up GPG signing credentials:
   ```bash
   export SIGNING_KEY_ID="your-key-id"
   export SIGNING_KEY_PASSWORD="your-key-password"
   export SIGNING_KEY_FILE="/path/to/your/key.gpg"
   ```
3. Run the deployment script:
   ```bash
   ./deploy-to-maven-central.sh [--auto-publish]
   ```
   The `--auto-publish` flag will automatically publish the artifacts after validation. Without this flag, you'll need to manually publish from the Sonatype Central Portal.

## Troubleshooting

If deployment fails:

1. Check the error message for specific issues
2. Optionally run the verification script to identify any missing requirements:
   ```bash
   ./verify_maven_central.sh
   ```
3. Try the deployment again

## Checking Deployment Status

You can check the status of your deployment at:
https://central.sonatype.com/publishing/deployments
