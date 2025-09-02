#!/bin/bash
# Fix name tags and packaging type in POM file
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  exit 1
fi

echo "Fixing name tags and packaging type in $POM_FILE"

# Create a backup of the original file
cp "$POM_FILE" "$POM_FILE.orig"

# Create a temporary directory for POM signature
TEMP_DIR=$(mktemp -d)
trap 'rm -rf "$TEMP_DIR"' EXIT

# Step 1: Fix name tags - replace <n> with <name>
echo "Step 1: Fixing name tags..."
sed -i.bak 's/<n>/<name>/g' "$POM_FILE" || echo "Failed to fix opening name tags"
sed -i.bak 's/<\/n>/<\/name>/g' "$POM_FILE" || echo "Failed to fix closing name tags"

# Fallback if sed doesn't work (for macOS compatibility)
if grep -q "<n>" "$POM_FILE"; then
  echo "Direct sed replacement failed, trying line-by-line replacement..."
  # Fix specific lines with name tags
  sed -i.bak '11s/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' "$POM_FILE"
  sed -i.bak '17s/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' "$POM_FILE"
  sed -i.bak '25s/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' "$POM_FILE"
fi

# Step 2: Fix packaging type - ensure it's aar not jar
echo "Step 2: Fixing packaging type..."
perl -i -pe 's/<packaging>jar<\/packaging>/<packaging>aar<\/packaging>/g' "$POM_FILE"

# Step 3: Fix plugin versions if needed
echo "Step 3: Fixing plugin versions..."
perl -i -pe 's|(<artifactId>central-publishing-maven-plugin</artifactId>\s*)<version>[^<]+</version>|\1<version>0.8.0</version>|g' "$POM_FILE"
perl -i -pe 's|(<artifactId>maven-gpg-plugin</artifactId>\s*)<version>[^<]+</version>|\1<version>3.2.8</version>|g' "$POM_FILE"

# Step 4: Fix dependency versions if needed
echo "Step 4: Fixing dependency versions..."
perl -i -pe 's|(<groupId>com\.google\.code\.gson</groupId>\s*<artifactId>gson</artifactId>\s*)<version>[^<]+</version>|\1<version>2.9.1</version>|g' "$POM_FILE"
perl -i -pe 's|(<groupId>com\.squareup\.okhttp3</groupId>\s*<artifactId>okhttp</artifactId>\s*)<version>[^<]+</version>|\1<version>4.8.0</version>|g' "$POM_FILE"

# Step 5: Verify the fixed POM
echo "Step 5: Verifying the fixed POM..."
echo "- Name tags:"
grep -n "<name>" "$POM_FILE" | head -3 || echo "No name tags found!"
if grep -q "<n>" "$POM_FILE"; then
  echo "WARNING: There are still <n> tags in the POM file!"
  grep -n "<n>" "$POM_FILE"
fi
echo "- Packaging type:"
grep -n "<packaging>" "$POM_FILE" || echo "No packaging type found!"
echo "- Plugin versions:"
grep -n "central-publishing-maven-plugin" -A 2 "$POM_FILE" || echo "Central plugin not found!"
grep -n "maven-gpg-plugin" -A 2 "$POM_FILE" || echo "GPG plugin not found!"
echo "- Dependencies:"
grep -n "<dependency>" -A 4 "$POM_FILE" | head -10 || echo "No dependencies found!"

echo "POM file fixed successfully!"