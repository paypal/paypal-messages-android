#!/bin/bash
# Script to fix name tags in POM files using direct file rewriting
set -e

# Function to fix a POM file's name tags
fix_pom_file() {
  local pom_file="$1"
  
  if [ ! -f "$pom_file" ]; then
    echo "File not found: $pom_file"
    return 1
  fi
  
  echo "Fixing POM file: $pom_file"
  
  # Read the file content
  content=$(<"$pom_file")
  
  # Replace the name tags
  content="${content//<n>/<name>}"
  content="${content//<\/n>/<\/name>}"
  
  # Write the content back to the file
  echo "$content" > "$pom_file"
  
  echo "Fixed name tags in: $pom_file"
  grep -n "<name>" "$pom_file" || echo "No name tags found!"
}

echo "Fixing POM files with direct string replacement..."

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

echo "POM files fixed successfully!"