#!/bin/bash
# Fix name tags closing tags in POM file
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  exit 1
fi

echo "Fixing name tags in $POM_FILE (plain text replacement)"

# Use plain text replacement to avoid escaping issues
cat "$POM_FILE" | sed 's/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' > "$POM_FILE.tmp"
cat "$POM_FILE.tmp" | sed 's/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' > "$POM_FILE.tmp2"
cat "$POM_FILE.tmp2" | sed 's/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' > "$POM_FILE.tmp"

# Copy back to original file
mv "$POM_FILE.tmp" "$POM_FILE"
rm -f "$POM_FILE.tmp2"

echo "Name tags fixed in $POM_FILE"
echo "Checking result:"
grep -n "<name>" "$POM_FILE" | head -3 || echo "No name tags found"
