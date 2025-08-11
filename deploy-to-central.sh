#!/bin/bash

# Script to deploy artifacts to Maven Central using the Central Portal Maven Plugin
set -e

echo "Deploying to Maven Central Portal..."

# Check for required environment variables
if [ -z "$SONATYPE_NEXUS_USERNAME" ] || [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_USERNAME and SONATYPE_NEXUS_PASSWORD must be set"
    exit 1
fi

# Prepare artifacts first
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"
GROUP_ID_PATH="com/paypal/messages"

echo "Deploying version: $VERSION"

# Change to the maven-deploy directory
cd library/build/maven-deploy

# The pom.xml already has the central-publishing-maven-plugin configured
# We just need to attach the artifacts and deploy

# Create a temporary pom with file references
cat > deploy-pom.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.paypal.messages</groupId>
    <artifactId>${ARTIFACT_ID}</artifactId>
    <version>${VERSION}</version>
    <packaging>pom</packaging>

    <name>PayPal Messages</name>
    <description>The PayPal Android SDK Messages Module: Promote offers to your customers such as Pay Later and PayPal Credit.</description>
    <url>https://github.com/paypal/paypal-messages-android</url>

    <licenses>
        <license>
            <name>The Apache License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        </license>
    </licenses>

    <developers>
        <developer>
            <id>paypal-messages-android</id>
            <name>PayPalMessages Android</name>
            <email>sdks-messages@paypal.com</email>
        </developer>
    </developers>

    <scm>
        <connection>scm:git:git://github.com/paypal/paypal-messages-android.git</connection>
        <developerConnection>scm:git:ssh://github.com:paypal/paypal-messages-android.git</developerConnection>
        <url>https://github.com/paypal/paypal-messages-android</url>
    </scm>

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
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# Sign artifacts and POM (Central requires GPG signatures)
echo "Signing artifacts and POM with GPG"
AAR_FILE="${ARTIFACT_ID}-${VERSION}.aar"
SRC_FILE="${ARTIFACT_ID}-${VERSION}-sources.jar"
POM_FILE="deploy-pom.xml"

# AAR signature
if [ -f "$AAR_FILE" ]; then
  gpg --batch --yes --armor --pinentry-mode loopback \
      --passphrase "${SIGNING_KEY_PASSWORD}" \
      --local-user "${SIGNING_KEY_ID}" \
      --detach-sign "$AAR_FILE"
else
  echo "Error: Missing AAR file: $AAR_FILE" >&2
  exit 1
fi

# Sources JAR signature
if [ -f "$SRC_FILE" ]; then
  gpg --batch --yes --armor --pinentry-mode loopback \
      --passphrase "${SIGNING_KEY_PASSWORD}" \
      --local-user "${SIGNING_KEY_ID}" \
      --detach-sign "$SRC_FILE"
else
  echo "Error: Missing sources JAR file: $SRC_FILE" >&2
  exit 1
fi

# POM signature (filename must be <artifactId>-<version>.pom.asc)
POM_ASC_DIR="target"
POM_ASC_NAME="${ARTIFACT_ID}-${VERSION}.pom.asc"
mkdir -p "$POM_ASC_DIR"
gpg --batch --yes --armor --pinentry-mode loopback \
    --passphrase "${SIGNING_KEY_PASSWORD}" \
    --local-user "${SIGNING_KEY_ID}" \
    --output "${POM_ASC_DIR}/${POM_ASC_NAME}" \
    --detach-sign "$POM_FILE"

# Pre-stage all components into the plugin's central-staging path so they are bundled
STAGE_DIR="target/central-staging/${GROUP_ID_PATH}/${ARTIFACT_ID}/${VERSION}"
mkdir -p "$STAGE_DIR"
# POM (the plugin will also copy it, but we ensure presence before bundling)
cp -f "$POM_FILE" "$STAGE_DIR/${ARTIFACT_ID}-${VERSION}.pom"
# POM signature
cp -f "${POM_ASC_DIR}/${POM_ASC_NAME}" "$STAGE_DIR/${POM_ASC_NAME}"
# AAR and signature
cp -f "$AAR_FILE" "$STAGE_DIR/${ARTIFACT_ID}-${VERSION}.aar"
cp -f "$AAR_FILE.asc" "$STAGE_DIR/${ARTIFACT_ID}-${VERSION}.aar.asc"
# Sources and signature
cp -f "$SRC_FILE" "$STAGE_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar"
cp -f "$SRC_FILE.asc" "$STAGE_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar.asc"

# Use Maven to invoke the Central Portal publish goal directly (avoids dependency resolution)
echo "Running Central Publishing plugin (publish goal)..."
mvn --batch-mode \
  -f deploy-pom.xml \
  -s ../../../.mvn/maven-settings.xml \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"