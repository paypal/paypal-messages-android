# Publishing to Maven Central

This project is configured to publish to Maven Central via the Sonatype Central Portal API.

## Prerequisites

1. Sonatype Central Portal account with access to publish under the `com.paypal` groupId
2. User token generated from Sonatype Central Portal
3. GPG/PGP key for signing artifacts

## Configuration

To publish the library, you need to provide the following credentials:

### Environment Variables

Set the following environment variables:

```bash
export SONATYPE_NEXUS_USERNAME=your_sonatype_username
export SONATYPE_NEXUS_PASSWORD=your_sonatype_user_token
export SIGNING_KEY_ID=your_gpg_key_id
export SIGNING_KEY_PASSWORD=your_gpg_key_password
export SIGNING_KEY_FILE=/path/to/secring.gpg
```

**Important**: `SONATYPE_NEXUS_PASSWORD` should be a user token generated from the Sonatype Central Portal. You can generate this token from your account page at https://central.sonatype.com/account.

## Publishing Method

### Central Portal API Publishing

To publish using the Central Portal API:

```bash
./gradlew publishToCentralPortal
```

By default, this will use the `USER_MANAGED` publishing type, which requires manual approval in the portal after validation. This is the recommended approach for official releases. 

To use automatic publishing (recommended for snapshots):

```bash
./gradlew publishToCentralPortal -PautoPublish
```

To check the status of your deployment:

```bash
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

### Central Portal API Issues
If you encounter issues with the Central Portal API:
1. Verify that your user token is correct and not expired
2. Check the response from the API for detailed error messages
3. Ensure your artifacts are properly signed
4. Verify that the package metadata (groupId, artifactId, version) is correct
5. Check the GitHub Actions logs for any credential or authentication issues

### Common Issues
- **"Component already exists" error**: This means you're trying to publish a version that already exists. Maven Central doesn't allow overwriting published artifacts. Use a new version number.
- **Missing manual approval**: For releases, you must manually approve the deployment by clicking the "Publish" button on the Central Portal deployments page: https://central.sonatype.com/publishing/deployments

### Testing Locally
When testing publishing locally, you may see warning messages about missing credentials. The build system will use dummy values for testing, which won't actually publish anything. This is expected behavior and helps with local testing without requiring real credentials.

To test with real credentials locally, set the environment variables as described in the Configuration section.