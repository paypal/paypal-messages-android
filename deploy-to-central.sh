#!/bin/bash

# Script to deploy artifacts to Maven Central via OSSRH using Gradle maven-publish (no @aar needed)
set -e

# Set this to true to skip POM XML name tag fixes (for debugging)
SKIP_POM_FIXES=${SKIP_POM_FIXES:-false}

echo "Deploying to Maven Central via OSSRH..."

# Check if we're using token authentication
if [ "${SONATYPE_TOKEN_AUTH:-false}" = "true" ]; then
    echo "Using token-based authentication..."
    # For token auth, we only need the token (stored in SONATYPE_NEXUS_PASSWORD)
    if [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
        echo "Error: Set SONATYPE_NEXUS_PASSWORD with your API token when using token authentication"
        exit 1
    fi
    
    # Set token auth flag for Gradle
    export SONATYPE_TOKEN_AUTH=true
    OSSRH_USER=""
    OSSRH_PASS="$SONATYPE_NEXUS_PASSWORD"
else
    echo "Using username/password authentication..."
    # Resolve OSSRH credentials (prefer OSSRH_*, fallback to SONATYPE_NEXUS_* for backward compat)
    OSSRH_USER="${OSSRH_USERNAME:-$SONATYPE_NEXUS_USERNAME}"
    OSSRH_PASS="${OSSRH_PASSWORD:-$SONATYPE_NEXUS_PASSWORD}"

    if [ -z "$OSSRH_USER" ] || [ -z "$OSSRH_PASS" ]; then
        echo "Error: Set OSSRH_USERNAME and OSSRH_PASSWORD (or SONATYPE_NEXUS_USERNAME/SONATYPE_NEXUS_PASSWORD)"
        exit 1
    fi
fi

# Prepare artifacts (ensures sources/javadoc jars exist for publication)
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

# Fix POM file name tags if needed
if [ "$SKIP_POM_FIXES" != "true" ]; then
    echo "Fixing POM XML name tags..."
    if [ -f "./fix_name_tags_mac.sh" ]; then
        ./fix_name_tags_mac.sh
    else
        echo "Warning: fix_name_tags_mac.sh not found, skipping POM fixes"
    fi
fi

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"

echo "Deploying version: $VERSION"

GRADLE_AUTH_PROPS=( -PsonatypeUsername="$OSSRH_USER" -PsonatypePassword="$OSSRH_PASS" )

# Publish to OSSRH (s01). Gradle maven-publish is configured with packaging=aar
# For releases (non -SNAPSHOT), this goes to staging; then we close and release the repository.
echo "Publishing to OSSRH staging (Gradle maven-publish)..."
./gradlew -q :library:publish "${GRADLE_AUTH_PROPS[@]}" | cat

echo "Closing and releasing staging repository..."
if [ "${SONATYPE_TOKEN_AUTH:-false}" = "true" ]; then
    echo "Using token-based repository close and release..."
    ./gradlew -q closeAndPromoteRepositoryWithToken "${GRADLE_AUTH_PROPS[@]}" | cat
else
    echo "Using standard repository close and release..."
    ./gradlew -q closeAndReleaseRepository "${GRADLE_AUTH_PROPS[@]}" | cat
fi

echo "Deployment initiated successfully!"
echo "Note: It can take 10–30 minutes to propagate to Maven Central mirrors."