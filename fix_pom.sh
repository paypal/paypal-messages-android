#!/bin/bash
# Script to fix name tags in XML files
set -e

# Check for arguments
if [ $# -eq 0 ]; then
  echo "Usage: $0 <file-to-fix>"
  echo "Example: $0 pom.xml"
  exit 1
fi

FILE_TO_FIX="$1"
if [ ! -f "$FILE_TO_FIX" ]; then
  echo "Error: File not found at $FILE_TO_FIX"
  exit 1
fi

echo "=== Fixing name tags in $FILE_TO_FIX ==="

# Create a backup
cp "$FILE_TO_FIX" "${FILE_TO_FIX}.bak"
echo "Created backup at ${FILE_TO_FIX}.bak"

# Use Perl for direct string replacement
perl -i -pe "s/<n>/<name>/g" "$FILE_TO_FIX"
perl -i -pe "s/<\/n>/<\/name>/g" "$FILE_TO_FIX"

# Verify fix worked
if grep -q "<n>" "$FILE_TO_FIX" || grep -q "</n>" "$FILE_TO_FIX"; then
  echo "WARNING: Still found name tag issues!"
  grep -n "<n>" "$FILE_TO_FIX" || true
  grep -n "</n>" "$FILE_TO_FIX" || true
  exit 1
else
  echo "✓ Successfully fixed all name tags!"
fi

echo "=== Fix complete! ==="
