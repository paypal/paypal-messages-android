#!/bin/bash

# Combined publishing script that tries traditional deploy first, then falls back to direct API if needed
set -e

echo "======================================================================================"
echo "PayPal Messages Android Library - Maven Central Publishing with Fallback"
echo "======================================================================================"

# Check for token in environment variables
if [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_PASSWORD environment variable is not set"
    echo "For token authentication, set SONATYPE_NEXUS_PASSWORD to your Sonatype API token"
    echo "You can create a token at: https://central.sonatype.com/profile"
    echo ""
    echo "Example: export SONATYPE_NEXUS_PASSWORD=your-token-here"
    exit 1
fi

# Get version info for logging
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Publishing version: $VERSION_FIXED"
echo "Artifact ID: $ARTIFACT_ID"
echo "Group ID: $GROUP_ID"

# First attempt: Traditional deployment through Gradle
echo "Step 1: Attempting deployment via Gradle maven-publish..."
./deploy-to-central.sh

# Check if the deployment was successful
if [ $? -eq 0 ]; then
    echo "Traditional deployment completed successfully!"
    exit 0
fi

# If we got here, there was a problem with the traditional deployment
echo ""
echo "======================================================================================"
echo "Traditional deployment had issues. Trying direct API publishing as fallback..."
echo "======================================================================================"
echo ""

# Second attempt: Direct token publishing
echo "Step 2: Attempting direct token-based API publishing..."
./publish-with-token.sh

if [ $? -eq 0 ]; then
    echo "Fallback direct publishing completed successfully!"
    exit 0
else
    echo "Both publishing methods failed. Please check error logs and credentials."
    exit 1
fi