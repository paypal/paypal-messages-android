#!/bin/bash

# Direct token-based API publishing to Maven Central for PayPal Messages Android library
set -e

# Ensure we have a token
if [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_PASSWORD must be set to your API token"
    exit 1
fi

# Prepare the artifacts first
echo "Preparing artifacts for Maven Central publishing..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Version: $VERSION_FIXED"
echo "Artifact ID: $ARTIFACT_ID"
echo "Group ID: $GROUP_ID"

# Directory with the prepared artifacts
ARTIFACTS_DIR="library/build/maven-deploy"

# Validate that we have all required artifacts
echo "Validating artifacts..."
for EXT in aar pom sources.jar javadoc.jar; do
    ARTIFACT_FILE="$ARTIFACTS_DIR/${ARTIFACT_ID}-${VERSION_FIXED}-${EXT}"
    if [ ! -f "$ARTIFACT_FILE" ]; then
        # Try without extension prefix for main artifacts
        if [ "$EXT" == "aar" ] || [ "$EXT" == "pom" ]; then
            ARTIFACT_FILE="$ARTIFACTS_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.${EXT}"
        fi
        
        if [ ! -f "$ARTIFACT_FILE" ]; then
            echo "Error: Required artifact not found: ${ARTIFACT_FILE}"
            exit 1
        fi
    fi
done

# Create a temporary directory for staging
STAGING_DIR="$ARTIFACTS_DIR/staging"
rm -rf "$STAGING_DIR"
mkdir -p "$STAGING_DIR"

# Group/artifact directory structure for API request
ARTIFACT_PATH="${GROUP_ID//./\/}/${ARTIFACT_ID}/${VERSION_FIXED}"
STAGING_ARTIFACT_DIR="$STAGING_DIR/$ARTIFACT_PATH"
mkdir -p "$STAGING_ARTIFACT_DIR"

# Copy and rename artifacts
echo "Preparing artifacts for upload..."
cp "$ARTIFACTS_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.aar" "$STAGING_ARTIFACT_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.aar"
cp "$ARTIFACTS_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom" "$STAGING_ARTIFACT_DIR/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
cp "$ARTIFACTS_DIR/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar" "$STAGING_ARTIFACT_DIR/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar"
cp "$ARTIFACTS_DIR/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar" "$STAGING_ARTIFACT_DIR/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar"

# Sign the artifacts if needed
echo "Checking for GPG signing..."
if [ -n "$SIGNING_KEY_ID" ] && [ -n "$SIGNING_KEY_PASSWORD" ] && [ -n "$SIGNING_KEY_FILE" ]; then
    echo "Signing artifacts..."
    for FILE in "$STAGING_ARTIFACT_DIR"/*; do
        gpg --batch --yes --armor --detach-sign \
            --default-key "$SIGNING_KEY_ID" \
            --passphrase "$SIGNING_KEY_PASSWORD" \
            --pinentry-mode loopback \
            "$FILE"
        
        if [ ! -f "${FILE}.asc" ]; then
            echo "Error: Failed to create signature for $FILE"
            exit 1
        fi
    done
else
    echo "Skipping GPG signing as credentials not provided"
fi

# Create zip file for upload
BUNDLE_ZIP="$STAGING_DIR/${ARTIFACT_ID}-${VERSION_FIXED}-bundle.zip"
echo "Creating bundle zip: $BUNDLE_ZIP"
cd "$STAGING_DIR"
zip -r "$BUNDLE_ZIP" .
cd - > /dev/null

# Upload to Central Portal using token
echo "Uploading to Maven Central via API..."
CENTRAL_API_URL="https://central.sonatype.com/api/v1/publisher/upload"
echo "POST $CENTRAL_API_URL"

# Use curl to upload
RESPONSE=$(curl -X POST "$CENTRAL_API_URL" \
    -H "Authorization: Bearer $SONATYPE_NEXUS_PASSWORD" \
    -F "bundle=@$BUNDLE_ZIP" \
    -s -w "%{http_code}")

HTTP_CODE=${RESPONSE: -3}
RESPONSE_BODY=${RESPONSE:0:${#RESPONSE}-3}

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "202" ]; then
    echo "Successfully uploaded to Maven Central!"
    echo "Response: $RESPONSE_BODY"
    
    # Extract deployment ID if available
    DEPLOYMENT_ID=$(echo "$RESPONSE_BODY" | grep -o '"deploymentId":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$DEPLOYMENT_ID" ]; then
        echo "Deployment ID: $DEPLOYMENT_ID"
        echo "Check status at: https://central.sonatype.com/publishing/deployments"
    fi
else
    echo "Failed to upload to Maven Central."
    echo "HTTP Code: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
    exit 1
fi

echo "Publication complete! It may take up to 30 minutes for artifacts to be available."