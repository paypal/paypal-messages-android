#!/bin/bash

# Script to deploy artifacts to Maven Central using wrapper POM approach
# This restores the working wrapper approach but fixes artifact inclusion
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
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Deploying version: $VERSION_FIXED"

# Stage artifacts into proper Maven-repo layout expected by the Central plugin
STAGING_ROOT="library/build/central-staging"
GROUP_PATH="com/paypal/messages/${ARTIFACT_ID}"
STAGE_DIR="${STAGING_ROOT}/${GROUP_PATH}/${VERSION_FIXED}"
SRC_DIR="library/build/maven-deploy"

rm -rf "${STAGING_ROOT}"
mkdir -p "${STAGE_DIR}"

# Copy library artifacts (THE KEY FIX: ensure these are included)
echo "Copying library artifacts to staging directory..."
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar" "${STAGE_DIR}/"

echo "Signing library artifacts..."
# Use our working CI signing helper
for file in "${STAGE_DIR}"/*; do
    if [[ -f "$file" && ! "$file" == *.asc ]]; then
        echo "Signing: $file"
        if [ -f "ci-sign-helper.sh" ] && [ -x "ci-sign-helper.sh" ]; then
            ./ci-sign-helper.sh "$file"
        fi
    fi
done

echo "Creating wrapper POM for Central plugin from template..."
WRAPPER_POM="${STAGING_ROOT}/deploy-pom.xml"
cp deploy-pom-template.xml "$WRAPPER_POM"

# Replace placeholders
sed -i.bak "s/PLACEHOLDER_GROUP_ID/${GROUP_ID}/g" "$WRAPPER_POM"
sed -i.bak "s/PLACEHOLDER_ARTIFACT_ID/${ARTIFACT_ID}/g" "$WRAPPER_POM"
sed -i.bak "s/PLACEHOLDER_VERSION/${VERSION_FIXED}/g" "$WRAPPER_POM"
rm -f "$WRAPPER_POM.bak"

# Fix XML name tags
echo "Fixing XML name tags in POM file..."
./fix_xml_during_deploy.sh "$WRAPPER_POM"
rm -f "$WRAPPER_POM.bak"

# Sign the wrapper POM
echo "Signing wrapper POM..."
if [ -f "ci-sign-helper.sh" ] && [ -x "ci-sign-helper.sh" ]; then
    ./ci-sign-helper.sh "$WRAPPER_POM"
fi

# Create target directory for Maven plugin
MAVEN_TARGET_REL="target/maven-bundle"
MAVEN_TARGET="${STAGING_ROOT}/${MAVEN_TARGET_REL}"
rm -rf "${MAVEN_TARGET}"
mkdir -p "${MAVEN_TARGET}"

# Copy ALL staged artifacts (library + wrapper) to target directory
echo "Copying all artifacts to Maven target directory..."
cp -r "${STAGING_ROOT}/com" "${MAVEN_TARGET}/"

# Also place wrapper POM in root for Maven to find
cp "${WRAPPER_POM}" "${MAVEN_TARGET}/"
if [ -f "${WRAPPER_POM}.asc" ]; then
    cp "${WRAPPER_POM}.asc" "${MAVEN_TARGET}/"
fi

echo "Final verification - listing all files that will be uploaded:"
find "${MAVEN_TARGET}" -type f | sort

# Run the Maven Central publish command
mvn --batch-mode \
  -f "${WRAPPER_POM}" \
  -s .mvn/maven-settings.xml \
  -DstagingDirectory="${MAVEN_TARGET_REL}" \
  -Dorg.slf4j.simpleLogger.log.org.sonatype.central=debug \
  verify \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"