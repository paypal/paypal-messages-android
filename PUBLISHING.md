# Publishing to Maven Central

This project is configured to publish to Maven Central via both the legacy Sonatype OSSRH system and the new Sonatype Central Portal API.

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

**Important**: For the Central Portal API, `SONATYPE_NEXUS_PASSWORD` should be a user token generated from the Sonatype Central Portal. You can generate this token from your account page at https://central.sonatype.com/account.

## Publishing Methods

### Method 1: Legacy OSSRH Publishing (Default)

To publish a release version using the legacy system:

```bash
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepositories
```

To publish a snapshot version:

1. Make sure your version in `build.gradle` ends with `-SNAPSHOT`
2. Run:

```bash
./gradlew publishToSonatype
```

### Method 2: Central Portal API Publishing (New)

To publish using the new Central Portal API:

```bash
./gradlew publishToCentralPortal
```

By default, this will use the `USER_MANAGED` publishing type, which requires manual approval in the portal after validation. To use automatic publishing:

```bash
./gradlew publishToCentralPortal -PautoPublish
```

To check the status of your deployment:

```bash
./gradlew checkCentralPortalDeployment
```

## CI/CD Publishing Configuration

The GitHub Actions workflows have been updated to support both publishing methods.

### Activating Central Portal Publishing

To use the Central Portal API for publishing in GitHub Actions, set the repository variable:

```
USE_CENTRAL_PORTAL = true
```

When this variable is not set or set to any other value, the legacy OSSRH publishing method will be used.

### How It Works

1. For snapshots (on `develop` branch), automatic publishing is enabled with the Central Portal API.
2. For releases (on `release` branch), user-managed publishing is used, requiring manual approval in the portal.

Both methods use the same secrets for authentication:
- `SONATYPE_NEXUS_USERNAME` / `SONATYPE_SDKS_NEXUS_USERNAME`
- `SONATYPE_NEXUS_PASSWORD` / `SONATYPE_SDKS_NEXUS_PASSWORD` (should be a user token)

## Repository URLs

### Legacy OSSRH URLs
- Release/Staging API: https://ossrh-staging-api.central.sonatype.com/service/local/
- Snapshots: https://central.sonatype.com/repository/maven-snapshots/

### Central Portal API URLs
- Upload Endpoint: https://central.sonatype.com/api/v1/publisher/upload
- Status Endpoint: https://central.sonatype.com/api/v1/publisher/status

## Authentication

- Legacy OSSRH: Uses basic authentication with username and password
- Central Portal API: Uses bearer token authentication with a user token

## Troubleshooting

### Legacy OSSRH Issues
If you encounter 401 Unauthorized errors:
1. Check that your Sonatype credentials are correct
2. Make sure your account has permission to publish to the `com.paypal` groupId
3. Verify that your credentials are properly set in environment variables

### Central Portal API Issues
If you encounter issues with the Central Portal API:
1. Verify that your user token is correct and not expired
2. Check the response from the API for detailed error messages
3. Ensure your artifacts are properly signed
4. Verify that the package metadata (groupId, artifactId, version) is correct