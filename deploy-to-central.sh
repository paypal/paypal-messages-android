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
# Fix duplicate SNAPSHOT suffix if present
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

# Copy artifacts (POM must have <packaging>aar</packaging>)
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar" "${STAGE_DIR}/"

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

sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar"
sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar"
sign_file "${STAGE_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar"

echo "Creating wrapper POM for Central plugin from template..."
WRAPPER_POM="${STAGING_ROOT}/deploy-pom.xml"
cp deploy-pom-template.xml "$WRAPPER_POM"

# Replace placeholders
sed -i.bak "s/PLACEHOLDER_GROUP_ID/${GROUP_ID}/g" "$WRAPPER_POM"
sed -i.bak "s/PLACEHOLDER_ARTIFACT_ID/${ARTIFACT_ID}/g" "$WRAPPER_POM"
sed -i.bak "s/PLACEHOLDER_VERSION/${VERSION_FIXED}/g" "$WRAPPER_POM"
# Don't set stagingDirectory in POM anymore as it's deprecated
# sed -i.bak "s|PLACEHOLDER_STAGING_ROOT|${STAGING_ROOT}|g" "$WRAPPER_POM"
rm -f "$WRAPPER_POM.bak"

# Fix XML name tags (direct replacement to avoid heredoc issues)
echo "Fixing XML name tags in POM file..."
./fix_xml_during_deploy.sh "$WRAPPER_POM"
rm -f "$WRAPPER_POM.bak"

# Sign the wrapper POM itself
echo "Signing wrapper POM file..."

# Sign the original wrapper POM first (important for Maven Central plugin)
sign_file "$WRAPPER_POM"

# Create directory structure expected by Central plugin
WRAPPER_ARTIFACT_DIR="${STAGING_ROOT}/com/paypal/messages/${ARTIFACT_ID}-central-publish/${VERSION_FIXED}"
mkdir -p "$WRAPPER_ARTIFACT_DIR"

# Copy to original artifact directory for inclusion in bundle
cp "$WRAPPER_POM" "$WRAPPER_ARTIFACT_DIR/${ARTIFACT_ID}-central-publish-${VERSION_FIXED}.pom"
cp "$WRAPPER_POM.asc" "$WRAPPER_ARTIFACT_DIR/${ARTIFACT_ID}-central-publish-${VERSION_FIXED}.pom.asc"

# Copy to library artifact directory as well (for completeness)
WRAPPER_POM_DST="${STAGING_ROOT}/${GROUP_PATH}/${VERSION_FIXED}/${ARTIFACT_ID}-central-publish-${VERSION_FIXED}.pom"
mkdir -p "$(dirname "$WRAPPER_POM_DST")"
cp "$WRAPPER_POM" "$WRAPPER_POM_DST"
cp "$WRAPPER_POM.asc" "$WRAPPER_POM_DST.asc"

echo "Wrapper POM signed and placed in all required locations"

# Publish via Central plugin using the staged Maven-repo layout
echo "Publishing via Maven Central Publishing plugin (stagingDirectory)..."
# Create a separate target directory to avoid nesting issues
TARGET_STAGING="${STAGING_ROOT}/target"
rm -rf "${TARGET_STAGING}"
mkdir -p "${TARGET_STAGING}"

# Create a separate bundle directory for Maven
MAVEN_TARGET="${TARGET_STAGING}/maven-bundle"
mkdir -p "${MAVEN_TARGET}"

# Copy the staged artifacts to the target directory
cp -r "${STAGING_ROOT}/com" "${MAVEN_TARGET}/"

# Sign the wrapper POM in its final Maven location
FINAL_POM="${MAVEN_TARGET}/com/paypal/messages/${ARTIFACT_ID}-central-publish/${VERSION_FIXED}/${ARTIFACT_ID}-central-publish-${VERSION_FIXED}.pom"
if [ -f "${FINAL_POM}" ]; then
  echo "Signing final POM at: ${FINAL_POM}"
  sign_file "${FINAL_POM}"
else
  echo "Warning: Final POM not found at expected location: ${FINAL_POM}"
  # Create the directory structure and copy the POM if it doesn't exist
  FINAL_POM_DIR="$(dirname "${FINAL_POM}")"
  mkdir -p "${FINAL_POM_DIR}"
  cp "${WRAPPER_POM}" "${FINAL_POM}"
  sign_file "${FINAL_POM}"
fi

# Run the Maven Central publish command with the new target directory
mvn --batch-mode \
  -f "${WRAPPER_POM}" \
  -s .mvn/maven-settings.xml \
  -DstagingDirectory=target/maven-bundle \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"
