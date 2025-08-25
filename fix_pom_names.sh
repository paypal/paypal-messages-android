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

# Create sed commands for name tags
cat > /tmp/fix_n_tags.sed << 'SEDCMD'
s/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g
s/<n>The Apache License, Version 2.0<\/n>/<name>The Apache License, Version 2.0<\/name>/g
s/<n>PayPalMessages Android<\/n>/<name>PayPalMessages Android<\/name>/g
SEDCMD

# Create sed commands for plugin versions
cat > /tmp/fix_plugin_versions.sed << 'SEDCMD'
s/<artifactId>central-publishing-maven-plugin<\/artifactId>.*<version>[^<]*<\/version>/<artifactId>central-publishing-maven-plugin<\/artifactId>\n                <version>0.8.0<\/version>/g
s/<artifactId>maven-gpg-plugin<\/artifactId>.*<version>[^<]*<\/version>/<artifactId>maven-gpg-plugin<\/artifactId>\n                <version>3.2.8<\/version>/g
SEDCMD

# Create sed commands for AAR extensions
cat > /tmp/fix_extensions.sed << 'SEDCMD'
/<build>/ {
a\
        <extensions>\
            <!-- For AAR packaging support -->\
            <extension>\
                <groupId>org.apache.maven.wagon</groupId>\
                <artifactId>wagon-http</artifactId>\
                <version>3.5.3</version>\
            </extension>\
            <extension>\
                <groupId>org.apache.maven.archetype</groupId>\
                <artifactId>archetype-packaging</artifactId>\
                <version>3.2.1</version>\
            </extension>\
        </extensions>
}
SEDCMD

# Apply all fixes
echo "Applying name tag fixes..."
sed -f /tmp/fix_n_tags.sed "$TMP_FILE" > "${TMP_FILE}.1"

echo "Applying plugin version fixes..."
sed -f /tmp/fix_plugin_versions.sed "${TMP_FILE}.1" > "${TMP_FILE}.2"

echo "Adding AAR packaging extensions..."
if ! grep -q "<extensions>" "${TMP_FILE}.2"; then
  sed -f /tmp/fix_extensions.sed "${TMP_FILE}.2" > "${TMP_FILE}.3"
else
  cp "${TMP_FILE}.2" "${TMP_FILE}.3"
  echo "Extensions section already exists, skipping..."
fi

# Copy fixed file back
cp "${TMP_FILE}.3" "$POM_FILE"

# Clean up
rm -f "$TMP_FILE" "${TMP_FILE}."* /tmp/fix_*.sed

echo "POM file fixed: $POM_FILE"
echo "Verifying name tags:"
grep -n "<name>" "$POM_FILE" || echo "No name tags found"
echo "Verifying plugin versions:"
grep -A 1 "central-publishing-maven-plugin" "$POM_FILE" | grep -E "version|central" || echo "Plugin version not found"
echo "Verifying extensions:"
grep -A 3 "<extensions>" "$POM_FILE" || echo "No extensions found"
