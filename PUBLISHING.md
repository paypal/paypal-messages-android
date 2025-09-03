# Publishing to Maven Central

This project is configured to publish to Maven Central via token-based authentication using the Central Portal Maven plugin.

## Prerequisites

1. Sonatype Central Portal account with access to publish under the `com.paypal` groupId
2. API token generated from Sonatype Central Portal
3. GPG/PGP key for signing artifacts (required for releases)

## Configuration

To publish the library, you need to provide the following credentials:

### Environment Variables

Set the following environment variables:

```bash
# Required: API token for authentication
export SONATYPE_NEXUS_PASSWORD=your_sonatype_api_token

# Required for releases: GPG signing keys
export SIGNING_KEY_ID=your_gpg_key_id
export SIGNING_KEY_PASSWORD=your_gpg_key_password
export SIGNING_KEY_FILE=/path/to/secring.gpg
```

**Important**: `SONATYPE_NEXUS_PASSWORD` should be an API token generated from the Sonatype Central Portal (`https://central.sonatype.com/profile`).

## Publishing Methods

### Recommended: Scripted Deployment

Use the unified deployment script that stages artifacts, signs them, and invokes the Central plugin:

```bash
./deploy-to-maven-central.sh [--auto-publish]
```

- Add `--auto-publish` to auto-publish after validation; omit for manual approval in the Portal.

### Gradle Tasks (advanced)

You can also use Gradle tasks to prepare artifacts:

```bash
./gradlew clean :library:assembleRelease :library:generatePomFileForReleasePublication
```

## CI/CD Publishing Configuration

The GitHub Actions workflows publish via the Central Portal API.

### Workflows

1. `release.yml` – manual releases
   - Decodes and imports the GPG key
   - Runs `deploy-to-maven-central.sh --no-auto-publish`
   - Requires manual approval in the Sonatype Central Portal

2. `release-snapshots.yml` – snapshot releases
   - Sets version with `-SNAPSHOT`
   - Publishes via composite action to the Central Portal
   - Uses automatic publishing (no manual approval)

Required secrets for both:
- `SONATYPE_NEXUS_USERNAME` – Sonatype username
- `SONATYPE_NEXUS_PASSWORD` – Sonatype user token
- `SIGNING_KEY_ID` – GPG key id
- `SIGNING_KEY_PASSWORD` – GPG key password
- `SIGNING_KEY_FILE` – base64-encoded GPG private key

## Manual Publishing Approval

When using manual (user-managed) mode, publish from the Portal after validation:

1. Open `https://central.sonatype.com/publishing/deployments`
2. Open your deployment
3. Click Publish

## Troubleshooting

### Authentication Errors (401)
- Ensure the API token is valid and configured

### "Component already exists"
- Use a new version or a `-SNAPSHOT` version for testing

### GPG signing issues
- Ensure the key is imported and `SIGNING_KEY_ID` matches a secret key fingerprint

## Checking Deployment Status

Monitor deployments at:
`https://central.sonatype.com/publishing/deployments`