#!/bin/bash

# Script to prepare artifacts for Maven Central publishing
set -e

echo "Preparing artifacts for Maven Central publishing..."

# Build the library
echo "Building library..."
./gradlew :library:assembleRelease :library:androidSourcesJar

# Get version from build.gradle
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Version: $VERSION"
echo "Artifact ID: $ARTIFACT_ID"
echo "Group ID: $GROUP_ID"

# Create target directory structure for Maven
TARGET_DIR="library/build/maven-deploy"
rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"

# Copy AAR file
echo "Copying AAR file..."
cp "library/build/outputs/aar/library-release.aar" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION}.aar"

# Copy sources JAR
echo "Copying sources JAR..."
SOURCES_JAR="library/build/libs/library-${VERSION}-sources.jar"
if [ -f "$SOURCES_JAR" ]; then
    cp "$SOURCES_JAR" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar"
else
    # Try alternative location
    SOURCES_JAR_ALT="library/build/intermediates/source_jar/release/release-sources.jar"
    if [ -f "$SOURCES_JAR_ALT" ]; then
        cp "$SOURCES_JAR_ALT" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar"
    else
        echo "Error: Sources JAR not found at expected locations"
        exit 1
    fi
fi

# Copy POM file (use the one we have)
echo "Copying POM file..."
cp "library/pom.xml" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION}.pom"

# Update version in the copied POM
sed -i.bak "s/<version>.*<\/version>/<version>${VERSION}<\/version>/g" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION}.pom"
rm "$TARGET_DIR/${ARTIFACT_ID}-${VERSION}.pom.bak"

echo "Artifacts prepared in: $TARGET_DIR"
echo "Contents:"
ls -la "$TARGET_DIR"

echo ""
echo "Ready for Maven Central publishing!"