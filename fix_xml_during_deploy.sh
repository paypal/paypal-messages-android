#!/bin/bash
# Helper script to fix XML tags during Maven Central deployment

# Takes the POM file path as an argument
POM_FILE="$1"

if [ -z "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file-path>"
  exit 1
fi

if [ ! -f "$POM_FILE" ]; then
  echo "Error: POM file not found: $POM_FILE"
  exit 1
fi

echo "Fixing XML name tags in $POM_FILE..."

# Create a temporary file
TMP_FILE=$(mktemp)

# Replace <n> with <name> tags
cat "$POM_FILE" | sed 's/<n>/<name>/g; s/<\/n>/<\/name>/g' > "$TMP_FILE"

# Check if the replacement worked
if grep -q "<name>" "$TMP_FILE"; then
  echo "Replacement successful - found <name> tags"
else
  echo "Warning: No <name> tags found after replacement"
fi

# Copy the fixed file back
cp "$TMP_FILE" "$POM_FILE"
rm -f "$TMP_FILE"

echo "XML tags fixed in $POM_FILE"