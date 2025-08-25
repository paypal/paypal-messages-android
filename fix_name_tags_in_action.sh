#!/bin/bash
# Fix name tags in POM file - Direct approach
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  exit 1
fi

echo "Fixing name tags in $POM_FILE"

# Use direct search and replace without any escaping issues
cp "$POM_FILE" "$POM_FILE.orig"

# Fix first name tag (line 11)
sed -i'.bak' '11s/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' "$POM_FILE"

# Fix second name tag (license, line 17)
sed -i'.bak' '17s/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' "$POM_FILE"

# Fix third name tag (developer, line 25)
sed -i'.bak' '25s/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' "$POM_FILE"

# Clean up
rm -f "$POM_FILE.bak"

echo "Name tags fixed in $POM_FILE"
echo "Checking result:"
echo "- Name tags:"
grep -n "<name>" "$POM_FILE" || echo "No name tags found"
