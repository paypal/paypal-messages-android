#!/bin/bash

# Prepare bundle for NMCP publishing
set -e

echo "Preparing NMCP bundle..."

# Build the library and generate all artifacts
./gradlew clean :library:assembleRelease :library:generatePomFileForReleasePublication

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"

echo "Version: $VERSION"
echo "Artifact ID: $ARTIFACT_ID"

# Create the bundle directory in library/build which is where NMCP expects it
BUNDLE_DIR="library/build/libs"
mkdir -p "$BUNDLE_DIR"

# Get the fixed version for naming
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
echo "Fixed version: $VERSION_FIXED"

# Copy and rename AAR file
echo "Copying AAR..."
cp "library/build/outputs/aar/library-release.aar" "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.aar"

# The POM should already be in library/build/pom.xml from our gradle task
if [ ! -f "library/build/pom.xml" ]; then
    echo "Error: POM file not found at library/build/pom.xml"
    exit 1
fi

# Also ensure POM is in the libs directory - fix for duplicate SNAPSHOT issue
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
cp "library/build/pom.xml" "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom"

# Update version in the POM file
sed -i.bak "s/<version>.*<\/version>/<version>${VERSION_FIXED}<\/version>/g" "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
rm "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom.bak"

# Create sources JAR
echo "Creating sources JAR..."
SRC_DIR="library/src/main"
SOURCES_JAR="$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar"

# Create a temporary directory for the sources
TEMP_SRC_DIR="library/build/tmp/nmcp-sources-${VERSION_FIXED}"
rm -rf "$TEMP_SRC_DIR"
mkdir -p "$TEMP_SRC_DIR"

# Copy the source files
if [ -d "$SRC_DIR/java" ]; then
    cp -r "$SRC_DIR/java/"* "$TEMP_SRC_DIR/"
fi

if [ -d "$SRC_DIR/kotlin" ]; then
    cp -r "$SRC_DIR/kotlin/"* "$TEMP_SRC_DIR/"
fi

# Create the sources JAR
jar cf "$SOURCES_JAR" -C "$TEMP_SRC_DIR" .

echo ""
echo "Bundle prepared. Contents of $BUNDLE_DIR:"
ls -la "$BUNDLE_DIR"

echo ""
echo "Ready for NMCP publishing. The nmcpPublishAggregationToCentralPortal task should now find all required files."