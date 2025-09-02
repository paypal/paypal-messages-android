#!/bin/bash
# Script to fix name tags in XML files with direct string replacement
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

# Fix opening and closing name tags with Perl
echo "Fixing name tags using perl..."
perl -i -pe 's/<n>/<name>/g' "$FILE_TO_FIX"
perl -i -pe 's/<\/n>/<\/name>/g' "$FILE_TO_FIX"

# Verify fix
if grep -q "<n>" "$FILE_TO_FIX" || grep -q "</n>" "$FILE_TO_FIX"; then
  echo "WARNING: Name tags still exist in the file!"
  echo "Trying targeted replacements..."

  # Try targeted replacements with key phrases
  perl -i -pe 's/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' "$FILE_TO_FIX"
  perl -i -pe 's/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' "$FILE_TO_FIX"
  perl -i -pe 's/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' "$FILE_TO_FIX"
  
  # Check again
  if grep -q "<n>" "$FILE_TO_FIX" || grep -q "</n>" "$FILE_TO_FIX"; then
    echo "WARNING: Still found name tag issues after targeted replacement!"
    grep -n "<n>" "$FILE_TO_FIX" || true
    grep -n "</n>" "$FILE_TO_FIX" || true
  else
    echo "✓ Successfully fixed name tags with targeted replacement!"
  fi
else
  echo "✓ Successfully fixed name tags with perl!"
fi

# Print summary
echo "=== File fix summary ==="
echo "- Name tags:"
if grep -q "<name>" "$FILE_TO_FIX"; then
  echo "✓ Found proper <name> tags"
else
  echo "WARNING: No <name> tags found after fixing!"
fi

echo "=== Name tag fix complete ==="