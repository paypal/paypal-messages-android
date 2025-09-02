#!/bin/bash
# Script to prepare Maven Central artifacts with proper metadata and signatures
set -e

VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
echo "Preparing Maven Central artifacts (version: $VERSION)"

# Step 1: Create root POM with proper metadata
echo "Step 1: Creating root POM..."
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

# Step 2: Create library module POM template
echo "Step 2: Creating library POM template..."
mkdir -p library/build/libs
cat > library/build/libs/paypal-messages.pom << EOF2
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.paypal.messages</groupId>
    <artifactId>paypal-messages</artifactId>
    <version>${VERSION}</version>
    <packaging>aar</packaging>

    <name>PayPal Messages</name>
    <description>The PayPal Android SDK Messages Module: Promote offers to your customers such as Pay Later and PayPal Credit.</description>
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

    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.9.1</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.8.0</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
EOF2

# Step 3: Create version-specific copy
echo "Step 3: Creating version-specific POMs..."
cp library/build/libs/paypal-messages.pom "library/build/libs/paypal-messages-${VERSION}.pom"

# Step 4: Create signature files
echo "Step 4: Creating signature files..."
if [ -n "$SIGNING_KEY_ID" ] && [ -n "$SIGNING_KEY_PASSWORD" ]; then
  echo "Using GPG to create signatures..."
  gpg --batch --yes --pinentry-mode loopback --passphrase "${SIGNING_KEY_PASSWORD}" \
      --local-user "${SIGNING_KEY_ID}" --armor --detach-sign \
      --output "pom.xml.asc" "pom.xml"
      
  gpg --batch --yes --pinentry-mode loopback --passphrase "${SIGNING_KEY_PASSWORD}" \
      --local-user "${SIGNING_KEY_ID}" --armor --detach-sign \
      --output "library/build/libs/paypal-messages-${VERSION}.pom.asc" "library/build/libs/paypal-messages-${VERSION}.pom"
else
  echo "Creating dummy signature files..."
  echo "DUMMY SIGNATURE FOR TESTING" > "pom.xml.asc"
  echo "DUMMY SIGNATURE FOR TESTING" > "library/build/libs/paypal-messages-${VERSION}.pom.asc"
fi

# Step 5: Create Maven settings file
echo "Step 5: Creating Maven settings file..."
mkdir -p .mvn
cat > .mvn/maven-settings.xml << EOF2
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>central</id>
            <username>\${env.SONATYPE_NEXUS_USERNAME}</username>
            <password>\${env.SONATYPE_NEXUS_PASSWORD}</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>gpg</id>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.keyname>\${env.SIGNING_KEY_ID}</gpg.keyname>
                <gpg.passphrase>\${env.SIGNING_KEY_PASSWORD}</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>gpg</activeProfile>
    </activeProfiles>
</settings>
EOF2

# Step 6: Create temporary directory for central-staging
echo "Step 6: Creating temporary central-staging directory..."
mkdir -p target/central-staging/com/paypal/messages/paypal-messages-parent/${VERSION}
cp pom.xml "target/central-staging/com/paypal/messages/paypal-messages-parent/${VERSION}/paypal-messages-parent-${VERSION}.pom"
cp pom.xml.asc "target/central-staging/com/paypal/messages/paypal-messages-parent/${VERSION}/paypal-messages-parent-${VERSION}.pom.asc"

mkdir -p target/central-staging/com/paypal/messages/paypal-messages/${VERSION}
cp "library/build/libs/paypal-messages-${VERSION}.pom" "target/central-staging/com/paypal/messages/paypal-messages/${VERSION}/paypal-messages-${VERSION}.pom"
cp "library/build/libs/paypal-messages-${VERSION}.pom.asc" "target/central-staging/com/paypal/messages/paypal-messages/${VERSION}/paypal-messages-${VERSION}.pom.asc"

echo "Maven Central artifacts prepared successfully!"
