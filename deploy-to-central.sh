#!/bin/bash

# Script to deploy artifacts to Maven Central via OSSRH using Gradle maven-publish (no @aar needed)
set -e

# Set this to true to skip POM XML name tag fixes (for debugging)
SKIP_POM_FIXES=${SKIP_POM_FIXES:-false}

echo "Deploying to Maven Central via OSSRH..."

# Check if we're using token authentication or if we need to auto-detect
echo "Checking authentication method..."
echo "SONATYPE_TOKEN_AUTH=${SONATYPE_TOKEN_AUTH:-not set}"
echo "OSSRH_USERNAME=${OSSRH_USERNAME:-not set}"
echo "SONATYPE_NEXUS_USERNAME=${SONATYPE_NEXUS_USERNAME:-not set}"

# Force token authentication by default now that Sonatype requires it
# Or explicitly set to true/false via environment variable
TOKEN_AUTH=true
if [ "${SONATYPE_TOKEN_AUTH:-}" = "false" ]; then
    TOKEN_AUTH=false
fi

if [ "$TOKEN_AUTH" = "true" ]; then
    echo "Using token-based authentication..."
    # For token auth, we only need the token (stored in SONATYPE_NEXUS_PASSWORD)
    if [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
        echo "Error: SONATYPE_NEXUS_PASSWORD environment variable is not set"
        echo "For token authentication, set SONATYPE_NEXUS_PASSWORD to your Sonatype API token"
        echo "You can create a token at: https://central.sonatype.com/profile"
        echo ""
        echo "Example: export SONATYPE_NEXUS_PASSWORD=your-token-here"
        exit 1
    fi
    
    # Set token auth flag for Gradle - export it so it's visible to subprocess Gradle invocations
    export SONATYPE_TOKEN_AUTH=true
    OSSRH_USER=""
    OSSRH_PASS="$SONATYPE_NEXUS_PASSWORD"
    echo "Token authentication is enabled. Token length: ${#SONATYPE_NEXUS_PASSWORD} characters"
else
    echo "Using username/password authentication..."
    # Resolve OSSRH credentials (prefer OSSRH_*, fallback to SONATYPE_NEXUS_* for backward compat)
    OSSRH_USER="${OSSRH_USERNAME:-$SONATYPE_NEXUS_USERNAME}"
    OSSRH_PASS="${OSSRH_PASSWORD:-$SONATYPE_NEXUS_PASSWORD}"

    if [ -z "$OSSRH_USER" ] || [ -z "$OSSRH_PASS" ]; then
        echo "Error: Set OSSRH_USERNAME and OSSRH_PASSWORD (or SONATYPE_NEXUS_USERNAME/SONATYPE_NEXUS_PASSWORD)"
        exit 1
    fi
    echo "Username authentication is enabled with user: $OSSRH_USER"
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
if [ "$TOKEN_AUTH" = "true" ]; then
    echo "Using token-based authentication for publishing..."
    ./gradlew -q :library:publish "${GRADLE_AUTH_PROPS[@]}" -PsonatypeTokenAuth=true | cat
else
    echo "Using standard authentication for publishing..."
    ./gradlew -q :library:publish "${GRADLE_AUTH_PROPS[@]}" | cat
fi

echo "Closing and releasing staging repository..."
# Use TOKEN_AUTH variable we set above for consistency
if [ "$TOKEN_AUTH" = "true" ]; then
    echo "Using token-based repository close and release..."
    echo "Token authentication flag: SONATYPE_TOKEN_AUTH=${SONATYPE_TOKEN_AUTH}"
    echo "Running closeAndPromoteRepositoryWithToken with token-based authentication..."
    # Run with full stack trace for better error reporting
    ./gradlew --stacktrace closeAndPromoteRepositoryWithToken "${GRADLE_AUTH_PROPS[@]}" -PsonatypeTokenAuth=true | cat
    
    # Check if the command failed
    if [ ${PIPESTATUS[0]} -ne 0 ]; then
        echo "Error: Failed to close and promote repository"
        echo "Check that your SONATYPE_NEXUS_PASSWORD token is valid and has the correct permissions"
        echo "If issues persist, try using the publish-with-token.sh script as an alternative"
    fi
else
    echo "Using standard repository close and release..."
    ./gradlew -q closeAndReleaseRepository "${GRADLE_AUTH_PROPS[@]}" | cat
fi

echo "Deployment initiated successfully!"
echo "Note: It can take 10–30 minutes to propagate to Maven Central mirrors."
echo ""
echo "If you encounter issues with the repository close and release process,"
echo "you can try the direct publishing method using:"
echo "  ./publish-with-token.sh"