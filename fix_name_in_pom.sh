#!/bin/bash
# Fix name tags in POM file for Maven Central
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  exit 1
fi

echo "Fixing name tags in $POM_FILE"

# Create a backup of the original file
cp "$POM_FILE" "$POM_FILE.orig"

# Directly replace specific lines using perl
echo "Replacing name tags directly with perl..."
perl -i -pe 's/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' "$POM_FILE"
perl -i -pe 's/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' "$POM_FILE"
perl -i -pe 's/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' "$POM_FILE"

# Verify the fix
echo "Verifying name tags fix..."
if grep -q "<n>" "$POM_FILE"; then
  echo "WARNING: There are still <n> tags in the POM file!"
  grep -n "<n>" "$POM_FILE"
  
  # Try alternative approach using sed
  echo "Trying alternative approach with sed..."
  sed -i.bak '11s/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' "$POM_FILE" || echo "Failed to fix first name tag"
  sed -i.bak '17s/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' "$POM_FILE" || echo "Failed to fix second name tag"
  sed -i.bak '25s/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' "$POM_FILE" || echo "Failed to fix third name tag"
  
  # Clean up backup files
  rm -f "$POM_FILE".bak
fi

# Check if fix was successful
if grep -q "<n>" "$POM_FILE"; then
  echo "WARNING: Failed to fix all name tags. Manual fix required!"
  grep -n "<n>" "$POM_FILE"
else
  echo "All name tags fixed successfully!"
fi

# Ensure packaging type is aar
echo "Ensuring packaging type is set to aar..."
sed -i.bak 's/<packaging>jar<\/packaging>/<packaging>aar<\/packaging>/g' "$POM_FILE" || echo "Failed to fix packaging type"
rm -f "$POM_FILE".bak

# Verify packaging type
echo "Verifying packaging type..."
grep -n "<packaging>" "$POM_FILE" || echo "No packaging type found!"

echo "POM file preparation complete."