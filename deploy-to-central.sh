#!/bin/bash

# Script to deploy PayPal Messages Android Library to Maven Central
# Uses the library POM directly - no wrapper POM needed
set -e

echo "=== DEPLOYING PAYPAL MESSAGES ANDROID LIBRARY TO MAVEN CENTRAL ==="

# Check for required environment variables
if [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_PASSWORD must be set to your API token or password"
    exit 1
fi

if [ -z "$SIGNING_KEY_ID" ] || [ -z "$SIGNING_KEY_PASSWORD" ]; then
    echo "Error: SIGNING_KEY_ID and SIGNING_KEY_PASSWORD must be set for GPG signing"
    exit 1
fi

# Prepare artifacts first (build AAR, sources, javadoc)
echo "Preparing library artifacts..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Deploying library: ${GROUP_ID}:${ARTIFACT_ID}:${VERSION_FIXED}"
echo "Primary artifact: AAR (Android Archive)"

# Use the library POM directly - it already has all the correct configuration
LIBRARY_POM="library/pom.xml"
LIBRARY_BUILD_DIR="library/build/maven-deploy"

# Verify artifacts exist
echo "Verifying artifacts exist..."
REQUIRED_FILES=(
    "${LIBRARY_BUILD_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar"
    "${LIBRARY_BUILD_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
    "${LIBRARY_BUILD_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar"
    "${LIBRARY_BUILD_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "ERROR: Required artifact missing: $file"
        exit 1
    fi
    echo "✓ Found: $(basename "$file")"
done

# Create a working directory for Maven operations
WORK_DIR="library/build/maven-central-deploy"
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"

# Copy the library POM to working directory
cp "$LIBRARY_POM" "$WORK_DIR/pom.xml"

# Update version in the copied POM to match the build
echo "Updating POM version to: $VERSION_FIXED"
sed -i.bak "s/<version>.*<\/version>/<version>$VERSION_FIXED<\/version>/g" "$WORK_DIR/pom.xml"
rm -f "$WORK_DIR/pom.xml.bak"

# Set up Maven to use our prepared artifacts
echo "Configuring Maven to use pre-built artifacts..."

# The library POM already has:
# - packaging=aar (AAR as primary artifact)
# - maven-gpg-plugin (for signing)
# - central-publishing-maven-plugin (for deployment)
# - All required metadata (name, description, licenses, developers, SCM)

echo ""
echo "=== MAVEN CENTRAL DEPLOYMENT ==="
echo "Using library POM with Central Publishing Maven Plugin"
echo "Maven will:"
echo "1. Use pre-built AAR from: ${LIBRARY_BUILD_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar"
echo "2. Sign all artifacts with GPG during 'verify' phase"
echo "3. Deploy to Maven Central via Central Publishing plugin"
echo ""

# Run Maven to deploy the library to Central
# The library POM is configured to deploy to Maven Central via the central-publishing-maven-plugin
mvn --batch-mode \
  -f "$WORK_DIR/pom.xml" \
  -s .mvn/maven-settings.xml \
  -DskipTests=true \
  -Dmaven.build.dir="$LIBRARY_BUILD_DIR" \
  -Dmaven.build.finalName="${ARTIFACT_ID}-${VERSION_FIXED}" \
  clean verify \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"
