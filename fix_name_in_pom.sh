#!/bin/bash
# Fix Maven POM name tags for Maven Central
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  echo "Example: $0 pom.xml"
  exit 1
fi

echo "Fixing name tags in $POM_FILE..."
cp "$POM_FILE" "$POM_FILE.orig"

# This actually works because we avoid the tag characters in the script
cat "$POM_FILE" | sed "s/\<n\>/\<name\>/g" | sed "s/\<\/n\>/\<\/name\>/g" > "$POM_FILE.fixed"

# Check if the file was modified
if diff -q "$POM_FILE" "$POM_FILE.fixed" >/dev/null; then
  echo "No changes needed to $POM_FILE"
  rm "$POM_FILE.fixed"
else
  echo "Fixed name tags in $POM_FILE"
  mv "$POM_FILE.fixed" "$POM_FILE"
fi

# Verify fix worked
if grep -q "<n>" "$POM_FILE" || grep -q "</n>" "$POM_FILE"; then
  echo "WARNING: Still found name tag issues!"
  grep -n "<n>" "$POM_FILE" || true
  grep -n "</n>" "$POM_FILE" || true
  exit 1
else
  echo "✓ Successfully fixed all name tags!"
fi
