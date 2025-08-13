#!/bin/bash
# Script to fix name tags in POM files for Maven Central publishing

echo "=== Fixing XML name tags in POM files ==="

fix_file() {
  local pom_file="$1"
  echo "Processing $pom_file"
  
  # Create backup
  cp "$pom_file" "$pom_file.bak"
  
  # Fix first name tag
  perl -i -pe 's/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' "$pom_file"
  
  # Fix second name tag
  perl -i -pe 's/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' "$pom_file"
  
  # Fix third name tag
  perl -i -pe 's/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' "$pom_file"
  
  echo "Fixed tags in $pom_file"
}

# Fix the template POM file
if [ -f "library/pom.xml" ]; then
  fix_file "library/pom.xml"
fi

# Fix generated POM files if they exist
if [ -f "library/build/publications/release/pom-default.xml" ]; then
  fix_file "library/build/publications/release/pom-default.xml"
fi

# Get current version from build.gradle
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
if [ -f "library/build/maven-deploy/paypal-messages-${VERSION}.pom" ]; then
  fix_file "library/build/maven-deploy/paypal-messages-${VERSION}.pom"
fi

echo "=== Name tag fixing complete ==="