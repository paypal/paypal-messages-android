#!/bin/bash

# Script to deploy artifacts to Maven Central via OSSRH using Gradle maven-publish (no @aar needed)
set -e

echo "Deploying to Maven Central via OSSRH..."

# Resolve OSSRH credentials (prefer OSSRH_*, fallback to SONATYPE_NEXUS_* for backward compat)
OSSRH_USER="${OSSRH_USERNAME:-$SONATYPE_NEXUS_USERNAME}"
OSSRH_PASS="${OSSRH_PASSWORD:-$SONATYPE_NEXUS_PASSWORD}"

if [ -z "$OSSRH_USER" ] || [ -z "$OSSRH_PASS" ]; then
    echo "Error: Set OSSRH_USERNAME and OSSRH_PASSWORD (or SONATYPE_NEXUS_USERNAME/SONATYPE_NEXUS_PASSWORD)"
    exit 1
fi

# Prepare artifacts (ensures sources/javadoc jars exist for publication)
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

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
./gradlew -q closeAndReleaseRepository "${GRADLE_AUTH_PROPS[@]}" | cat

echo "Deployment initiated successfully!"
echo "Note: It can take 10–30 minutes to propagate to Maven Central mirrors."