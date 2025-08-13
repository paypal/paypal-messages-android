#!/bin/bash

# Script to deploy artifacts to Maven Central via OSSRH using Gradle maven-publish (no @aar needed)
set -e

echo "Deploying to Maven Central via OSSRH..."

# Check for required environment variables (basic auth for OSSRH)
if [ -z "$SONATYPE_NEXUS_USERNAME" ] || [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_USERNAME and SONATYPE_NEXUS_PASSWORD must be set"
    exit 1
fi

# Prepare artifacts (ensures sources/javadoc jars exist for publication)
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"

echo "Deploying version: $VERSION"

# Publish to OSSRH (s01). Gradle maven-publish is configured with packaging=aar
# For releases (non -SNAPSHOT), this goes to staging; then we close and release the repository.
echo "Publishing to OSSRH staging (Gradle maven-publish)..."
./gradlew -q :library:publish | cat

echo "Closing and releasing staging repository..."
./gradlew -q closeAndReleaseRepository | cat

echo "Deployment initiated successfully!"
echo "Note: It can take 10–30 minutes to propagate to Maven Central mirrors."