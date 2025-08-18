# Publishing to Maven Central

This project is configured to publish to Maven Central via token-based authentication. It supports both the Sonatype OSSRH API and the Central Portal API.

## Prerequisites

1. Sonatype Central Portal account with access to publish under the `com.paypal` groupId
2. API token generated from Sonatype Central Portal
3. GPG/PGP key for signing artifacts (optional for snapshots)

## Configuration

To publish the library, you need to provide the following credentials:

### Environment Variables

Set the following environment variables:

```bash
# Required: API token for authentication (token-based auth is now the default)
export SONATYPE_NEXUS_PASSWORD=your_sonatype_api_token

# Optional: GPG signing keys (required for releases)
export SIGNING_KEY_ID=your_gpg_key_id
export SIGNING_KEY_PASSWORD=your_gpg_key_password
export SIGNING_KEY_FILE=/path/to/secring.gpg
```

**Important**: `SONATYPE_NEXUS_PASSWORD` should be an API token generated from the Sonatype Central Portal. You can generate this token from your account page at https://central.sonatype.com/profile.

## Publishing Methods

We provide several methods for publishing to Maven Central:

### 1. Combined Publishing (Recommended)

The most reliable approach that tries multiple publishing methods with fallback mechanisms:

```bash
./publish-with-fallback.sh
```

This script will:
1. Try traditional Gradle maven-publish with token authentication
2. If that fails, automatically fall back to direct API publishing
3. Provide detailed error messages if issues occur

### 2. Traditional Maven Publishing

For publishing via the traditional Sonatype OSSRH workflow:

```bash
./deploy-to-central.sh
```

This uses Gradle's maven-publish plugin with token authentication to publish to Sonatype OSSRH and then close and release the repository.

### 3. Direct API Publishing

For publishing directly to the Sonatype Central Portal API:

```bash
./publish-with-token.sh
```

This uses a direct HTTP API approach to upload the bundle to Central Portal without using the Gradle maven-publish plugin.

### 4. Gradle Tasks

You can also use the Gradle tasks directly:

```bash
# Direct API publishing via Central Portal
./gradlew publishToCentralPortal

# For automatic publishing (recommended for snapshots)
./gradlew publishToCentralPortal -PautoPublish

# Traditional publishing via OSSRH
./gradlew :library:publish -PsonatypeTokenAuth=true

# Check deployment status
./gradlew checkCentralPortalDeployment
```

## CI/CD Publishing Configuration

The GitHub Actions workflows have been updated to support both publishing methods.

### GitHub Actions Publishing

The repository is configured to use the Central Portal API for publishing in GitHub Actions. Make sure you've set up the required secrets as described in the "Setting Up GitHub Repository Secrets" section below.

### How It Works

The repository has two distinct workflows:

1. **For snapshots:** Uses the `auto_publish: 'true'` setting to enable automatic publishing via the Central Portal API.
   - No manual approval is needed - artifacts go straight to Maven Central after validation
   - Used for development builds and quick testing

2. **For releases:** Uses the `auto_publish: 'true'` setting (default) for user-managed publishing.
   - Requires manual approval in the Sonatype Central Portal after validation
   - Provides an opportunity to verify artifacts before they are published to Maven Central
   - Recommended for official releases

### Publishing Workflows

The repository includes two main publishing workflows:

1. **release.yml** - Used for official releases
   - Triggered manually via workflow_dispatch
   - Creates a GitHub release
   - Publishes to Maven Central using USER_MANAGED mode (auto_publish: 'false')
   - Requires manual approval in the Sonatype Central Portal

2. **release-snapshots.yml** - Used for snapshot releases
   - Triggered manually via workflow_dispatch
   - Sets version with -SNAPSHOT suffix
   - Publishes to Maven Central using AUTOMATIC mode (auto_publish: 'true')
   - No manual approval needed

Both methods use the same secrets for authentication:
- `SONATYPE_NEXUS_USERNAME` - Your Sonatype username
- `SONATYPE_NEXUS_PASSWORD` - Your Sonatype user token

## Manual Publishing Approval

When using the `USER_MANAGED` publishing mode (default for releases), you need to manually approve the deployment after validation:

1. After the GitHub Action completes the upload, it will show a success message with a link to the Sonatype Central Portal
2. Go to the Sonatype Central Portal deployments page: https://central.sonatype.com/publishing/deployments
3. Find your deployment in the list (it will show as "PUBLISHING" status)
4. Click on the deployment to view details
5. After reviewing the artifacts, click the "Publish" button to finalize the publishing process
6. Your artifacts will then be published to Maven Central (this may take a few hours to appear in all indices)

![Publish Button Location](https://central.sonatype.com/publishing/deployments)

## Repository URLs

### Central Portal API URLs
- Upload Endpoint: https://central.sonatype.com/api/v1/publisher/upload
- Status Endpoint: https://central.sonatype.com/api/v1/publisher/status
- Deployments Page: https://central.sonatype.com/publishing/deployments

## Authentication

- Central Portal API: Uses bearer token authentication with a user token

### Setting Up GitHub Repository Secrets

To publish from GitHub Actions, you need to set up these repository secrets:

1. Go to your GitHub repository → Settings → Secrets and variables → Actions
2. Add the following repository secrets:
   - `SONATYPE_NEXUS_USERNAME`: Your Sonatype username
   - `SONATYPE_NEXUS_PASSWORD`: Your Sonatype user token (from Central Portal)
   - `SIGNING_KEY_ID`: Your GPG key ID
   - `SIGNING_KEY_PASSWORD`: Your GPG key password
   - `SIGNING_KEY_FILE`: Your GPG private key (base64 encoded)

## Troubleshooting

### Common Issues

1. **Authentication Errors (401)**
   - Make sure you're using a valid API token
   - Ensure the token has the correct permissions
   - Try regenerating your token at https://central.sonatype.com/profile
   - Check that SONATYPE_NEXUS_PASSWORD is correctly set

2. **SSL/TLS Errors**
   - Try using the direct API publishing method (`./publish-with-token.sh`)
   - This may bypass SSL/TLS issues that can occur with Gradle

3. **"Component already exists" error**
   - This means you're trying to publish a version that already exists
   - Maven Central doesn't allow overwriting published artifacts
   - Use a new version number or append a SNAPSHOT suffix for development

4. **Missing manual approval**
   - For releases using USER_MANAGED mode, you must manually approve the deployment
   - Click the "Publish" button on the Central Portal deployments page: 
     https://central.sonatype.com/publishing/deployments

5. **OkHttp Dependency Issues**
   - The library now bundles OkHttp 4.8.0 as an API dependency
   - ProGuard rules have been added to prevent conflicts with other libraries
   - When integrated with Braintree or other libraries that use OkHttp 3.x, no conflicts should occur

### Dependency Conflict Resolution

The AAR is now the primary artifact and bundles OkHttp correctly:
- OkHttp 4.8.0 is included as an `api` dependency
- ProGuard rules in `consumer-rules.pro` prevent conflicts
- No need to use the `@aar` suffix when depending on the library

### Testing Locally

When testing publishing locally:
- You may see warning messages about missing credentials if environment variables aren't set
- The scripts will fail gracefully with helpful error messages
- Use a test token for trying out the publishing process without actually deploying

For proper local testing:
1. Set the environment variables as described in the Configuration section
2. Use a `-SNAPSHOT` suffix for test versions
3. Try the combined publishing script: `./publish-with-fallback.sh`