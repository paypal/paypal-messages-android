#!/bin/bash

# Script to prepare artifacts for Maven Central publishing
set -e

echo "Preparing artifacts for Maven Central publishing..."

# Build the library
echo "Building library..."
./gradlew :library:assembleRelease

# Get version from build.gradle
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
# Fix duplicate SNAPSHOT suffix if present
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Original Version: $VERSION"
echo "Fixed Version: $VERSION_FIXED"
echo "Artifact ID: $ARTIFACT_ID"
echo "Group ID: $GROUP_ID"

# Create target directory structure for Maven
TARGET_DIR="library/build/maven-deploy"
rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"

# Copy AAR file
echo "Copying AAR file..."
cp "library/build/outputs/aar/library-release.aar" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.aar"

# Create sources JAR manually
echo "Creating sources JAR..."
SOURCES_DIR="library/build/tmp/sources-${VERSION_FIXED}"
rm -rf "$SOURCES_DIR"
mkdir -p "$SOURCES_DIR"
# Copy source files
cp -r library/src/main/java "$SOURCES_DIR/"
cp -r library/src/main/kotlin "$SOURCES_DIR/" 2>/dev/null || true
# Create JAR
jar cf "${TARGET_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar" -C "$SOURCES_DIR" .

# Create a minimal Javadoc JAR (placeholder) to satisfy Central requirements
echo "Creating Javadoc JAR (placeholder)..."
JAVADOC_DIR="library/build/tmp/javadoc-${VERSION_FIXED}"
rm -rf "$JAVADOC_DIR"
mkdir -p "$JAVADOC_DIR/META-INF"
cat > "$JAVADOC_DIR/README.md" << JDOC
This is a placeholder Javadoc JAR for ${ARTIFACT_ID} ${VERSION_FIXED}.
For API documentation, please visit: https://github.com/paypal/paypal-messages-android
JDOC
jar cf "${TARGET_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar" -C "$JAVADOC_DIR" .

# Copy POM file (use the one we have)
echo "Copying POM file..."
cp "library/pom.xml" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom"

# Update version in the copied POM
sed -i.bak "s/<version>.*<\/version>/<version>${VERSION_FIXED}<\/version>/g" "$TARGET_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
rm "$TARGET_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom.bak"

echo "Artifacts prepared in: $TARGET_DIR"
echo "Contents:"
ls -la "$TARGET_DIR"

echo ""
echo "Ready for Maven Central publishing!"