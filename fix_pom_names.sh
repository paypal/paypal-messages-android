#!/bin/bash
# Fix name tags in POM file
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  exit 1
fi

echo "Creating fixed POM with proper name tags from $POM_FILE"
TMP_FILE=$(mktemp)

# Create one-liner commands for each substitution to avoid path/special char issues
echo "Fixing name tags..."
cat "$POM_FILE" > "$TMP_FILE"

echo 's/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' > /tmp/sed_cmd1
echo 's/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g' > /tmp/sed_cmd2
echo 's/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g' > /tmp/sed_cmd3

# Run the substitutions
sed -f /tmp/sed_cmd1 "$TMP_FILE" > "${TMP_FILE}.1"
sed -f /tmp/sed_cmd2 "${TMP_FILE}.1" > "${TMP_FILE}.2"
sed -f /tmp/sed_cmd3 "${TMP_FILE}.2" > "${TMP_FILE}.3"

# Fix plugin versions
echo "Fixing plugin versions..."
echo 's/<artifactId>central-publishing-maven-plugin<\/artifactId>.*<version>1.1.7<\/version>/<artifactId>central-publishing-maven-plugin<\/artifactId>\n                <version>0.8.0<\/version>/g' > /tmp/sed_cmd4
echo 's/<artifactId>maven-gpg-plugin<\/artifactId>.*<version>1.1.7<\/version>/<artifactId>maven-gpg-plugin<\/artifactId>\n                <version>3.2.8<\/version>/g' > /tmp/sed_cmd5

sed -f /tmp/sed_cmd4 "${TMP_FILE}.3" > "${TMP_FILE}.4"
sed -f /tmp/sed_cmd5 "${TMP_FILE}.4" > "${TMP_FILE}.5"

# Copy fixed file back
cp "${TMP_FILE}.5" "$POM_FILE"

# Clean up
rm -f "$TMP_FILE" "${TMP_FILE}."* /tmp/sed_cmd*

echo "POM file fixed: $POM_FILE"
