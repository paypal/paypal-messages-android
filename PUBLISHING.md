# Publishing to Maven Central

This project is configured to publish to Maven Central via Sonatype Central Portal.

## Prerequisites

1. Sonatype Central Portal account with access to publish under the `com.paypal` groupId
2. GPG/PGP key for signing artifacts

## Configuration

To publish the library, you need to provide the following credentials:

### Option 1: Environment Variables

Set the following environment variables:

```bash
export SONATYPE_NEXUS_USERNAME=your_sonatype_username
export SONATYPE_NEXUS_PASSWORD=your_sonatype_password
export SIGNING_KEY_ID=your_gpg_key_id
export SIGNING_KEY_PASSWORD=your_gpg_key_password
export SIGNING_KEY_FILE=/path/to/secring.gpg
```

### Option 2: Gradle Properties

Add the following properties to your `~/.gradle/gradle.properties` file:

```properties
sonatypeUsername=your_sonatype_username
sonatypePassword=your_sonatype_password
signingKeyId=your_gpg_key_id
signingPassword=your_gpg_key_password
signingSecretKeyRingFile=/path/to/secring.gpg
```

## Publishing

To publish a release version:

```bash
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepositories
```

To publish a snapshot version:

1. Make sure your version in `build.gradle` ends with `-SNAPSHOT`
2. Run:

```bash
./gradlew publishToSonatype
```

## Repository URLs

The library is configured to use the Sonatype Central Portal API:

- Release URL: https://central.sonatype.com/api/v1/publisher/upload?name=PaypalMessages
- Snapshot URL: https://central.sonatype.com/api/v1/publisher/upload?name=PaypalMessages&publishingType=AUTOMATIC

Regular releases require manual approval in the Sonatype Central Portal UI, while snapshot releases use the AUTOMATIC publishing type to publish immediately after validation.

## Troubleshooting

If you encounter 401 Unauthorized errors:
1. Check that your Sonatype credentials are correct
2. Make sure your account has permission to publish to the `com.paypal` groupId
3. Verify that your credentials are properly set either in environment variables or gradle.properties