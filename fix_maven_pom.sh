#!/bin/bash
# Fix POM files before publication to Maven Central
set -e

echo "Fixing POM files for Maven Central publication..."

# Fix the generated POM file from Gradle
GRADLE_POM="library/build/publications/release/pom-default.xml"
if [ -f "$GRADLE_POM" ]; then
  echo "Fixing Gradle-generated POM: $GRADLE_POM"
  
  # Create backup
  cp "$GRADLE_POM" "$GRADLE_POM.bak"
  
  # Fix name tags with direct string replacement using perl
  perl -i -pe 's/<n>/<name>/g' "$GRADLE_POM"
  perl -i -pe 's/<\/n>/<\/name>/g' "$GRADLE_POM"
  
  # Clean up backup files
  rm -f "$GRADLE_POM.sed"
  
  echo "Fixed Gradle POM:"
  grep -n "<name>" "$GRADLE_POM" || echo "No name tags found!"
  
  # Copy to build/pom.xml for reference
  cp "$GRADLE_POM" "library/build/pom.xml"
fi

# Fix the Maven local repository POM as well
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
MAVEN_LOCAL_POM="$HOME/.m2/repository/com/paypal/messages/paypal-messages/$VERSION/paypal-messages-$VERSION.pom"

if [ -f "$MAVEN_LOCAL_POM" ]; then
  echo "Fixing Maven local repository POM: $MAVEN_LOCAL_POM"
  
  # Create backup
  cp "$MAVEN_LOCAL_POM" "$MAVEN_LOCAL_POM.bak"
  
  # Fix name tags with direct string replacement using perl
  perl -i -pe 's/<n>/<name>/g' "$MAVEN_LOCAL_POM"
  perl -i -pe 's/<\/n>/<\/name>/g' "$MAVEN_LOCAL_POM"
  
  # Clean up backup files
  rm -f "$MAVEN_LOCAL_POM.sed"
  
  echo "Fixed Maven local POM:"
  grep -n "<name>" "$MAVEN_LOCAL_POM" || echo "No name tags found!"
fi

echo "POM files fixed for Maven Central publication."