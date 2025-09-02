#!/bin/bash
# Fix Maven Central POM files just before deployment
set -e

echo "===== Maven Central POM Fix Script ====="

# Create parent POM with proper metadata
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
echo "Using version: $VERSION"

echo "Creating parent POM with proper metadata..."
cat > pom.xml << EOF2
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.paypal.messages</groupId>
    <artifactId>paypal-messages-parent</artifactId>
    <version>${VERSION}</version>
    <packaging>pom</packaging>
    
    <name>PayPal Messages Parent</name>
    <description>Parent POM for PayPal Messages Android SDK</description>
    <url>https://github.com/paypal/paypal-messages-android</url>
    
    <licenses>
        <license>
            <name>The Apache License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        </license>
    </licenses>
    
    <developers>
        <developer>
            <id>paypal-messages-android</id>
            <name>PayPalMessages Android</name>
            <email>sdks-messages@paypal.com</email>
        </developer>
    </developers>
    
    <scm>
        <connection>scm:git:git://github.com/paypal/paypal-messages-android.git</connection>
        <developerConnection>scm:git:ssh://github.com:paypal/paypal-messages-android.git</developerConnection>
        <url>https://github.com/paypal/paypal-messages-android</url>
    </scm>
</project>
EOF2

# Create a signature for it
echo "Creating signature for parent POM..."
if [ -n "$SIGNING_KEY_ID" ] && [ -n "$SIGNING_KEY_PASSWORD" ]; then
  gpg --batch --yes --pinentry-mode loopback --passphrase "${SIGNING_KEY_PASSWORD}" \
      --local-user "${SIGNING_KEY_ID}" --armor --detach-sign \
      --output "pom.xml.asc" "pom.xml"
else
  echo "DUMMY SIGNATURE FOR TESTING" > pom.xml.asc
fi

# Fix library POM file
echo "Fixing library POM file..."
LIB_POM="library/build/libs/paypal-messages-${VERSION}.pom"
if [ -f "$LIB_POM" ]; then
  echo "Found library POM: $LIB_POM"
  
  # Make a backup
  cp "$LIB_POM" "${LIB_POM}.bak"
  
  # Fix name tags with literal string replacement
  echo "Fixing name tags..."
  perl -i -pe 's/<n>/<name>/g' "$LIB_POM"
  perl -i -pe 's/<\/n>/<\/name>/g' "$LIB_POM"
  
  # Check if fix worked
  if grep -q "<n>" "$LIB_POM" || grep -q "</n>" "$LIB_POM"; then
    echo "WARNING: Name tags still exist in library POM!"
    grep -n "<n>" "$LIB_POM" || true
    grep -n "</n>" "$LIB_POM" || true
  else
    echo "✓ Successfully fixed name tags in library POM"
  fi
  
  # Recreate signature
  echo "Recreating signature for library POM..."
  if [ -n "$SIGNING_KEY_ID" ] && [ -n "$SIGNING_KEY_PASSWORD" ]; then
    gpg --batch --yes --pinentry-mode loopback --passphrase "${SIGNING_KEY_PASSWORD}" \
        --local-user "${SIGNING_KEY_ID}" --armor --detach-sign \
        --output "${LIB_POM}.asc" "$LIB_POM"
  else
    echo "DUMMY SIGNATURE FOR TESTING" > "${LIB_POM}.asc"
  fi
else
  echo "Library POM not found at: $LIB_POM"
fi

# Monkey patch the deployment script to create proper parent POM
echo "Patching deployment script..."
if [ -f "deploy-to-maven-central.sh" ]; then
  cp deploy-to-maven-central.sh deploy-to-maven-central.sh.bak
  
  # Replace the <n> tags in the template
  perl -i -pe 's/<n>/<name>/g' "deploy-to-maven-central.sh"
  perl -i -pe 's/<\/n>/<\/name>/g' "deploy-to-maven-central.sh"
  
  # Replace the minimal parent POM with a proper one with metadata
  perl -i -pe 'BEGIN{undef $/;} s/cat > pom.xml << EOF\n<\?xml.*?<\/project>\nEOF/cat > pom.xml << EOF\n<\?xml version="1.0" encoding="UTF-8"\?>\n<project xmlns="http:\/\/maven.apache.org\/POM\/4.0.0" xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance"\n         xsi:schemaLocation="http:\/\/maven.apache.org\/POM\/4.0.0 http:\/\/maven.apache.org\/xsd\/maven-4.0.0.xsd">\n    <modelVersion>4.0.0<\/modelVersion>\n    <groupId>com.paypal.messages<\/groupId>\n    <artifactId>paypal-messages-parent<\/artifactId>\n    <version>\${VERSION}<\/version>\n    <packaging>pom<\/packaging>\n    \n    <name>PayPal Messages Parent<\/name>\n    <description>Parent POM for PayPal Messages Android SDK<\/description>\n    <url>https:\/\/github.com\/paypal\/paypal-messages-android<\/url>\n    \n    <licenses>\n        <license>\n            <name>The Apache License, Version 2.0<\/name>\n            <url>http:\/\/www.apache.org\/licenses\/LICENSE-2.0<\/url>\n        <\/license>\n    <\/licenses>\n    \n    <developers>\n        <developer>\n            <id>paypal-messages-android<\/id>\n            <name>PayPalMessages Android<\/name>\n            <email>sdks-messages@paypal.com<\/email>\n        <\/developer>\n    <\/developers>\n    \n    <scm>\n        <connection>scm:git:git:\/\/github.com\/paypal\/paypal-messages-android.git<\/connection>\n        <developerConnection>scm:git:ssh:\/\/github.com:paypal\/paypal-messages-android.git<\/developerConnection>\n        <url>https:\/\/github.com\/paypal\/paypal-messages-android<\/url>\n    <\/scm>\n<\/project>\nEOF/gs' "deploy-to-maven-central.sh"
  
  # Do the same for the second POM creation
  perl -i -pe 'BEGIN{undef $/;} s/cat > pom.xml << EOF\n<\?xml.*?<\/project>\nEOF/cat > pom.xml << EOF\n<\?xml version="1.0" encoding="UTF-8"\?>\n<project xmlns="http:\/\/maven.apache.org\/POM\/4.0.0" xmlns:xsi="http:\/\/www.w3.org\/2001\/XMLSchema-instance"\n         xsi:schemaLocation="http:\/\/maven.apache.org\/POM\/4.0.0 http:\/\/maven.apache.org\/xsd\/maven-4.0.0.xsd">\n    <modelVersion>4.0.0<\/modelVersion>\n    <groupId>com.paypal.messages<\/groupId>\n    <artifactId>paypal-messages-parent<\/artifactId>\n    <version>\${VERSION}<\/version>\n    <packaging>pom<\/packaging>\n    \n    <name>PayPal Messages Parent<\/name>\n    <description>Parent POM for PayPal Messages Android SDK<\/description>\n    <url>https:\/\/github.com\/paypal\/paypal-messages-android<\/url>\n    \n    <licenses>\n        <license>\n            <name>The Apache License, Version 2.0<\/name>\n            <url>http:\/\/www.apache.org\/licenses\/LICENSE-2.0<\/url>\n        <\/license>\n    <\/licenses>\n    \n    <developers>\n        <developer>\n            <id>paypal-messages-android<\/id>\n            <name>PayPalMessages Android<\/name>\n            <email>sdks-messages@paypal.com<\/email>\n        <\/developer>\n    <\/developers>\n    \n    <scm>\n        <connection>scm:git:git:\/\/github.com\/paypal\/paypal-messages-android.git<\/connection>\n        <developerConnection>scm:git:ssh:\/\/github.com:paypal\/paypal-messages-android.git<\/developerConnection>\n        <url>https:\/\/github.com\/paypal\/paypal-messages-android<\/url>\n    <\/scm>\n<\/project>\nEOF/gs' "deploy-to-maven-central.sh"
  
  echo "✓ Patched deployment script"
else
  echo "Deployment script not found"
fi

echo "===== Maven Central POM fix completed ====="
