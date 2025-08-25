#!/bin/bash

# Script to deploy artifacts to Maven Central using library POM directly
# This approach ensures the AAR packaging type is preserved
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

echo "Using library POM directly for deployment..."
# Use the library POM directly instead of creating a wrapper POM
LIBRARY_POM="${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
echo "Library POM: ${LIBRARY_POM}"

# Fix the POM file to ensure it has proper name tags and extensions for AAR packaging
echo "Fixing POM file for Maven Central..."

# Fix name tags and plugin versions using direct string replacements
echo "Fixing name and plugin tags..."
{
  # Read the POM file
  POM_CONTENT=$(cat "$LIBRARY_POM")
  
  # Replace name tags
  POM_CONTENT=$(echo "$POM_CONTENT" | sed 's/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g')
  POM_CONTENT=$(echo "$POM_CONTENT" | sed 's/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g')
  POM_CONTENT=$(echo "$POM_CONTENT" | sed 's/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g')
  
  # Fix plugin versions
  POM_CONTENT=$(echo "$POM_CONTENT" | sed 's/<artifactId>central-publishing-maven-plugin<\/artifactId>.*<version>[^<]*<\/version>/<artifactId>central-publishing-maven-plugin<\/artifactId>\n                <version>0.8.0<\/version>/g')
  POM_CONTENT=$(echo "$POM_CONTENT" | sed 's/<artifactId>maven-gpg-plugin<\/artifactId>.*<version>[^<]*<\/version>/<artifactId>maven-gpg-plugin<\/artifactId>\n                <version>3.2.8<\/version>/g')
  
  # Write back to the POM file
  echo "$POM_CONTENT" > "$LIBRARY_POM"
}

# Add extensions for AAR support if needed
echo "Adding AAR support extensions..."
if ! grep -q "<extensions>" "$LIBRARY_POM"; then
    echo "Extensions section not found, adding it..."
    TMPFILE=$(mktemp)
    sed '/<build>/ a\
    <extensions>\
        <!-- For AAR packaging support -->\
        <extension>\
            <groupId>org.apache.maven.wagon</groupId>\
            <artifactId>wagon-http</artifactId>\
            <version>3.5.3</version>\
        </extension>\
        <extension>\
            <groupId>org.apache.maven.archetype</groupId>\
            <artifactId>archetype-packaging</artifactId>\
            <version>3.2.1</version>\
        </extension>\
    </extensions>' "$LIBRARY_POM" > "$TMPFILE"
    mv "$TMPFILE" "$LIBRARY_POM"
else
    echo "Extensions section already exists, using it."
fi

echo "POM file fixed successfully!"

# Create target directory for Maven plugin
MAVEN_TARGET_REL="target/maven-bundle"
MAVEN_TARGET="${STAGING_ROOT}/${MAVEN_TARGET_REL}"
rm -rf "${MAVEN_TARGET}"
mkdir -p "${MAVEN_TARGET}"

# Copy all staged artifacts to target directory
echo "Copying all artifacts to Maven target directory..."
cp -r "${STAGING_ROOT}/com" "${MAVEN_TARGET}/"

# Use the library POM directly for deployment - no wrapper POM needed
echo "The library POM already has packaging=aar and will be the primary artifact"

echo "Final verification - listing all files that will be uploaded:"
find "${MAVEN_TARGET}" -type f | sort

# Run the Maven Central publish command
# Use absolute path for staging directory to ensure plugin finds all artifacts
ABSOLUTE_MAVEN_TARGET=$(realpath "${MAVEN_TARGET}")
echo "Using absolute staging directory: ${ABSOLUTE_MAVEN_TARGET}"

mvn --batch-mode \
  -f "${LIBRARY_POM}" \
  -s .mvn/maven-settings.xml \
  -DstagingDirectory="${ABSOLUTE_MAVEN_TARGET}" \
  -Dorg.slf4j.simpleLogger.log.org.sonatype.central=debug \
  verify \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"
