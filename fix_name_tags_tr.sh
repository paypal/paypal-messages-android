#!/bin/bash
# Script to fix name tags in POM files using tr command
set -e

fix_pom_file() {
  local pom_file="$1"
  
  if [ ! -f "$pom_file" ]; then
    echo "File not found: $pom_file"
    return 1
  fi
  
  echo "Fixing POM file: $pom_file"
  
  # Create a temporary file
  local temp_file="${pom_file}.fixed"
  
  # Use tr to replace n with name in tags
  cat "$pom_file" | tr '<n>' '<name>' | tr '</n>' '</name>' > "$temp_file"
  
  # Replace the original file
  mv "$temp_file" "$pom_file"
  
  echo "Fixed POM file with tr command: $pom_file"
}

echo "Fixing POM files using tr command..."

# Fix the generated POM file
GRADLE_POM="library/build/publications/release/pom-default.xml"
if [ -f "$GRADLE_POM" ]; then
  fix_pom_file "$GRADLE_POM"
  
  # Copy to build/pom.xml for reference
  cp "$GRADLE_POM" "library/build/pom.xml"
  echo "Copied fixed POM to: library/build/pom.xml"
fi

# Fix the Maven local repository POM
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
MAVEN_POM="$HOME/.m2/repository/com/paypal/messages/paypal-messages/$VERSION/paypal-messages-$VERSION.pom"
if [ -f "$MAVEN_POM" ]; then
  fix_pom_file "$MAVEN_POM"
fi

echo "POM files fixed with tr command successfully!"