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

# Fix central plugin version (lines 41-42)
sed -i'.bak' '42s/<version>1.1.7<\/version>/<version>0.8.0<\/version>/g' "$POM_FILE"

# Fix gpg plugin version (lines 63-64)
sed -i'.bak' '64s/<version>1.1.7<\/version>/<version>3.2.8<\/version>/g' "$POM_FILE"

# Fix dependency versions (gson)
sed -i'.bak' '87s/<version>1.1.7<\/version>/<version>2.9.1<\/version>/g' "$POM_FILE"

# Fix dependency versions (okhttp)
sed -i'.bak' '93s/<version>1.1.7<\/version>/<version>4.8.0<\/version>/g' "$POM_FILE"

# Clean up
rm -f "$POM_FILE.bak"

echo "Name tags fixed in $POM_FILE"
echo "Checking result:"
echo "- Name tags:"
grep -n "<name>" "$POM_FILE" || echo "No name tags found"
echo "- Plugin versions:"
grep -n "central-publishing-maven-plugin" -A 2 "$POM_FILE" || echo "No central plugin found"
grep -n "maven-gpg-plugin" -A 2 "$POM_FILE" || echo "No gpg plugin found"
