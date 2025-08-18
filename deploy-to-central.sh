#!/bin/bash

# Script to deploy artifacts to Maven Central using the Central Publishing Maven Plugin (token-based)
set -e

echo "Deploying to Maven Central Portal..."

# Check for required environment variables
if [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_PASSWORD must be set to your API token or password"
    exit 1
fi

# Prepare artifacts first (build AAR, sources, javadoc, and POM)
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Deploying version: $VERSION"

# Stage artifacts into proper Maven-repo layout expected by the Central plugin
STAGING_ROOT="library/build/central-staging"
GROUP_PATH="com/paypal/messages/${ARTIFACT_ID}"
STAGE_DIR="${STAGING_ROOT}/${GROUP_PATH}/${VERSION}"
SRC_DIR="library/build/maven-deploy"

rm -rf "${STAGING_ROOT}"
mkdir -p "${STAGE_DIR}"

# Copy artifacts (POM must have <packaging>aar</packaging>)
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION}.pom" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION}.aar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION}-sources.jar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION}-javadoc.jar" "${STAGE_DIR}/"

echo "Signing artifacts for Central validation..."
sign_file() {
  local f="$1"
  if [ -f "$f" ] && [ ! -f "$f.asc" ]; then
    local PASSPHRASE=${MAVEN_GPG_PASSPHRASE:-$SIGNING_KEY_PASSWORD}
    local UID_ARGS=()
    if [ -n "$SIGNING_KEY_ID" ]; then UID_ARGS+=(--local-user "$SIGNING_KEY_ID"); fi
    if [ -n "$PASSPHRASE" ]; then
      printf '%s' "$PASSPHRASE" | gpg --batch --yes --pinentry-mode loopback --passphrase-fd 0 "${UID_ARGS[@]}" --armor --detach-sign "$f"
    else
      gpg --batch --yes "${UID_ARGS[@]}" --armor --detach-sign "$f"
    fi
  fi
}

sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION}.pom"
sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION}.aar"
sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION}-sources.jar"
sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION}-javadoc.jar"

echo "Creating wrapper POM for Central plugin..."
WRAPPER_POM="${STAGING_ROOT}/deploy-pom.xml"
cat > "${WRAPPER_POM}" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${GROUP_ID}</groupId>
  <artifactId>${ARTIFACT_ID}-central-publish</artifactId>
  <version>${VERSION}</version>
  <packaging>pom</packaging>
  <name>Central Publish Wrapper</name>
  <build>
    <plugins>
      <plugin>
        <groupId>org.sonatype.central</groupId>
        <artifactId>central-publishing-maven-plugin</artifactId>
        <version>0.8.0</version>
        <extensions>true</extensions>
        <configuration>
          <publishingServerId>central</publishingServerId>
          <autoPublish>false</autoPublish>
          <waitUntil>validated</waitUntil>
          <deploymentName>PayPal Messages Android ${VERSION}</deploymentName>
          <stagingDirectory>${STAGING_ROOT}</stagingDirectory>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
EOF

# Publish via Central plugin using the staged Maven-repo layout
echo "Publishing via Maven Central Publishing plugin (stagingDirectory)..."
mvn --batch-mode \
  -f "${WRAPPER_POM}" \
  -s .mvn/maven-settings.xml \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"