# Maven Central Deployment Guide

This document outlines the process for deploying the PayPal Messages Android SDK to Maven Central. It addresses common issues and provides solutions to ensure successful deployment.

## Requirements for Maven Central

Maven Central has strict requirements for artifact submissions:

1. POM files must have proper XML formatting
2. All `<n>` tags must be `<name>` tags (common XML issue)
3. Required metadata in POM:
   - Project name
   - Project description
   - Project URL
   - License information
   - SCM information
   - Developers information
4. Signature files for all artifacts (*.asc)

## Fix Scripts

The following scripts are provided to fix common issues:

### 1. Comprehensive Fix Script

To fix all issues at once, run:

```bash
./fix_maven_central.sh
```

This script will:
- Fix name tags in all POM files
- Create a proper root POM with all required metadata
- Fix GitHub Action YML files if needed
- Create signatures for all POM files
- Verify all requirements are met

### 2. Individual Fix Scripts

If you need to fix specific issues:

#### Fix Name Tags in POM Files

```bash
./fix_name_in_pom.sh <pom-file>
```

Replaces `<n>` tags with `<name>` tags in the specified POM file.

#### Create Root POM

```bash
./fix_pom_in_action_mac.sh
```

Creates a proper root POM file with all required metadata for Maven Central.

#### Fix GitHub Action YML Files

```bash
./fix_action_yml.sh [action-yml-file]
```

Fixes name tags in GitHub Action YML files. If no file is specified, it defaults to `.github/actions/publish_maven_central/action.yml`.

#### Create POM Signatures

```bash
./fix_pom_signatures.sh [pom-file1] [pom-file2] ...
```

Creates signature files for POM files. If no files are specified, it tries to find POM files in common locations.

#### Verify Maven Central Requirements

```bash
./verify_maven_central.sh [pom-file1] [pom-file2] ...
```

Verifies that all Maven Central requirements are met for the specified POM files.

## Common Issues

### 1. Missing Signature Files

If you see this error:
```
Missing signature for file: paypal-messages-parent-1.1.10.pom
```

Run the signature fix script:
```bash
./fix_pom_signatures.sh
```

### 2. Missing Metadata

If you see errors about missing name, description, URL, license, SCM, or developers information, run:
```bash
./fix_pom_in_action_mac.sh
```

### 3. Name Tag Issues

If you see issues with XML formatting, specifically `<n>` tags, run:
```bash
./fix_name_in_pom.sh <pom-file>
```

## Deployment Process

1. Fix any issues with the POM files using the scripts above
2. Set up credentials for Sonatype Central Portal:
   ```bash
   export SONATYPE_NEXUS_USERNAME="your-username"
   export SONATYPE_NEXUS_PASSWORD="your-password"
   ```
3. Set up GPG signing credentials:
   ```bash
   export SIGNING_KEY_ID="your-key-id"
   export SIGNING_KEY_PASSWORD="your-key-password"
   export SIGNING_KEY_FILE="/path/to/your/key.gpg"
   ```
4. Run the deployment script:
   ```bash
   ./deploy-to-maven-central.sh [--auto-publish]
   ```
   The `--auto-publish` flag will automatically publish the artifacts after validation. Without this flag, you'll need to manually publish from the Sonatype Central Portal.

## Troubleshooting

If deployment fails:

1. Check the error message for specific issues
2. Run the verification script to identify any missing requirements:
   ```bash
   ./verify_maven_central.sh
   ```
3. Fix the issues using the appropriate fix scripts
4. Try the deployment again

## Checking Deployment Status

You can check the status of your deployment at:
https://central.sonatype.com/publishing/deployments