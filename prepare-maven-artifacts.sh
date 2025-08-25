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

# Update project version in the copied POM
GENERATED_POM="$TARGET_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
echo "Updating project version to ${VERSION_FIXED} in POM..."
sed -i.bak "s/<version>[^<]*<\/version>/<version>${VERSION_FIXED}<\/version>/" "$GENERATED_POM" 2>/dev/null || echo "Warning: Version replacement failed"
grep -n "<version>" "$GENERATED_POM" | head -1 || echo "No version tag found"

# Run the POM fixer script to correct name tags and plugin versions
if [ -f "./fix_pom_for_central.sh" ]; then
  echo "Running POM fixer script..."
  ./fix_pom_for_central.sh "$GENERATED_POM"
elif [ -f "./fix_name_tags.sh" ]; then
  echo "Running name tag fixer script..."
  ./fix_name_tags.sh "$GENERATED_POM"
else
  echo "Warning: POM fixer scripts not found. Manual fix may be required."
  # Try to fix name tags directly
  echo "Attempting to fix name tags directly..."
  sed -i.bak 's/<n>/<name>/g' "$GENERATED_POM"
  sed -i.bak 's/<\/n>/<\/name>/g' "$GENERATED_POM"
fi

rm -f "$GENERATED_POM.bak"

echo "Artifacts prepared in: $TARGET_DIR"
echo "Contents:"
ls -la "$TARGET_DIR"

echo ""
echo "Ready for Maven Central publishing!"
