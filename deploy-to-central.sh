#!/bin/bash

# Script to deploy artifacts to Maven Central using the Central Publishing Maven Plugin (token-based)
set -e

echo "Deploying to Maven Central Portal..."

# Check for required environment variables
if [ -z "$SONATYPE_NEXUS_USERNAME" ] || [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_USERNAME and SONATYPE_NEXUS_PASSWORD must be set"
    exit 1
fi

# Prepare artifacts first (build AAR, sources, and POM)
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"
STAGING_DIR="library/build/maven-deploy"

echo "Deploying version: $VERSION"

echo "Signing artifacts (.pom, .aar, -sources.jar)..."
# Sign artifacts so Central validation passes
sign_file() {
  local file_path="$1"
  if [ -f "$file_path" ] && [ ! -f "$file_path.asc" ]; then
    echo "  - $(basename "$file_path")"
    local PASSPHRASE=${MAVEN_GPG_PASSPHRASE:-$SIGNING_KEY_PASSWORD}
    local GPG_UID_ARGS=()
    if [ -n "$SIGNING_KEY_ID" ]; then
      GPG_UID_ARGS=(--local-user "$SIGNING_KEY_ID")
    fi
    if [ -n "$PASSPHRASE" ]; then
      # Non-interactive (CI) mode
      printf '%s' "$PASSPHRASE" | gpg --batch --yes --pinentry-mode loopback --passphrase-fd 0 "${GPG_UID_ARGS[@]}" --armor --detach-sign "$file_path"
    else
      # Interactive or agent-backed
      gpg --batch --yes "${GPG_UID_ARGS[@]}" --armor --detach-sign "$file_path"
    fi
  fi
}

sign_file "$STAGING_DIR/${ARTIFACT_ID}-${VERSION}.pom"
sign_file "$STAGING_DIR/${ARTIFACT_ID}-${VERSION}.aar"
sign_file "$STAGING_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar"

# Create a minimal wrapper POM (packaging=pom) that directs the Central plugin to our staging directory
WRAPPER_DIR="$STAGING_DIR/central-publish"
mkdir -p "$WRAPPER_DIR"
cat > "$WRAPPER_DIR/deploy-pom.xml" << EOF
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
          <autoPublish>true</autoPublish>
          <waitUntil>validated</waitUntil>
          <deploymentName>PayPal Messages Android ${VERSION}</deploymentName>
          <stagingDirectory>${STAGING_DIR}</stagingDirectory>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
EOF

echo "Publishing via Maven Central Publishing plugin..."
mvn --batch-mode \
  -f "$WRAPPER_DIR/deploy-pom.xml" \
  -s .mvn/maven-settings.xml \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"