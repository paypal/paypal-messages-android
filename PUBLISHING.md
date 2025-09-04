# Publishing to Maven Central

This project releases exclusively via GitHub Actions using the Sonatype Central Portal Maven plugin. Do not run local scripts to publish.

## Prerequisites (GitHub Secrets)

The following repository secrets must be configured in GitHub for the workflows to run:

- `SONATYPE_NEXUS_USERNAME` – Sonatype username
- `SONATYPE_NEXUS_PASSWORD` – Sonatype API token (Central Portal)
- `SIGNING_KEY_ID` – GPG key id
- `SIGNING_KEY_PASSWORD` – GPG key password
- `SIGNING_KEY_FILE` – base64-encoded GPG private key

## Release Workflows

### 1) Production Release (`.github/workflows/release.yml`)

- Trigger: Manual (`workflow_dispatch`)
- Jobs: `lint` → `test` → `build` → `release`
- Key steps in `release` job:
  - Decodes and imports the signing key via `./.github/actions/decode_signing_key_action`
  - Sets `USE_SNAPSHOT=false`
  - Runs the direct deployment script: `./deploy-to-maven-central.sh --no-auto-publish`
  - After validation completes, publish from the Sonatype Central Portal manually

When to use: production-ready versions that should be manually approved in the Portal.

### 2) Snapshot Release (`.github/workflows/release-snapshots.yml`)

- Trigger: Manual (`workflow_dispatch`)
- Jobs: `lint` → `test` → `build` → `release`
- Key steps in `release` job:
  - Decodes and imports the signing key via `./.github/actions/decode_signing_key_action`
  - Sets `USE_SNAPSHOT=true`
  - Runs `semantic-release` to determine a new snapshot version
  - Publishes via composite action `./.github/actions/publish_maven_central` with `use_snapshot: true` and `auto_publish: true`

When to use: snapshot builds for validation/testing that publish automatically.

## Manual Publishing Approval (Production Releases)

For production releases, after the CI deployment validates on Sonatype, publish from the Portal:

1. Open `https://central.sonatype.com/publishing/deployments`
2. Open your deployment
3. Click Publish

## Post-Release: Check Deployment Status

Monitor deployments at:

`https://central.sonatype.com/publishing/deployments`

Artifacts typically appear on Maven Central after validation and publication completes.

## Troubleshooting

- Authentication (401): verify `SONATYPE_NEXUS_USERNAME` and `SONATYPE_NEXUS_PASSWORD` secrets are set and valid.
- GPG signing issues: ensure the GPG secrets are present and the key decodes/imports correctly (handled by the decode action).
- Component already exists: bump the version (handled automatically for snapshots; for releases, ensure the version is new).

## Notes

- The composite action `./.github/actions/publish_maven_central` uses the Central Portal Maven plugin to publish artifacts prepared by the build.
- The decode action `./.github/actions/decode_signing_key_action` writes the provided base64 key to the runner for signing.