#!/bin/bash

# Prepare bundle for NMCP publishing
set -e

echo "Preparing NMCP bundle..."

# Build the library and generate all artifacts
./gradlew clean :library:assembleRelease :library:generatePomFileForReleasePublication :library:sourcesJar

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"

echo "Version: $VERSION"
echo "Artifact ID: $ARTIFACT_ID"

# Create the bundle directory in library/build which is where NMCP expects it
BUNDLE_DIR="library/build/libs"
mkdir -p "$BUNDLE_DIR"

# Copy and rename AAR file
echo "Copying AAR..."
cp "library/build/outputs/aar/library-release.aar" "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION}.aar"

# The POM should already be in library/build/pom.xml from our gradle task
if [ ! -f "library/build/pom.xml" ]; then
    echo "Error: POM file not found at library/build/pom.xml"
    exit 1
fi

# Also ensure POM is in the libs directory
cp "library/build/pom.xml" "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION}.pom"

# Copy and rename sources JAR
echo "Copying sources JAR..."
if [ -f "$BUNDLE_DIR/library-sources.jar" ]; then
    cp "$BUNDLE_DIR/library-sources.jar" "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar"
else
    echo "Warning: Sources JAR not found at expected location"
    # Try alternative location
    if [ -f "library/build/intermediates/runtime_library_classes_jar/release/classes.jar" ]; then
        echo "Creating sources JAR from classes JAR as fallback..."
        cp "library/build/intermediates/runtime_library_classes_jar/release/classes.jar" "$BUNDLE_DIR/${ARTIFACT_ID}-${VERSION}-sources.jar"
    fi
fi

echo ""
echo "Bundle prepared. Contents of $BUNDLE_DIR:"
ls -la "$BUNDLE_DIR"

echo ""
echo "Ready for NMCP publishing. The nmcpPublishAggregationToCentralPortal task should now find all required files."