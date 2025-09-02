#!/bin/bash
# Script to test the standard Maven publishing setup
set -e

echo "Testing standard Maven publishing..."

# Display available publication tasks
echo "Available publication tasks:"
./gradlew tasks --group=publishing

# Test generating publications without actually publishing
echo ""
echo "Generating publications without publishing..."
./gradlew :library:generateMetadataFileForReleasePublication

# Check the generated POM file
POM_DIR="library/build/publications/release"
if [ -d "$POM_DIR" ]; then
  echo ""
  echo "Generated POM file:"
  cat "$POM_DIR/pom-default.xml"
else
  echo ""
  echo "POM directory not found at: $POM_DIR"
  echo "Checking alternative locations..."
  find library/build -name "pom-default.xml" -type f
fi

echo ""
echo "Testing publish to local repository..."
./gradlew :library:publishToMavenLocal

echo ""
echo "Publishing to local repository completed."
echo "Check the local repository at: ~/.m2/repository/com/paypal/messages/paypal-messages/"