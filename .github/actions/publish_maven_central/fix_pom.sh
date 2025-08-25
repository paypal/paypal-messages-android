#!/bin/bash
# Fix POM file for Maven Central with a direct line-based approach
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  exit 1
fi

echo "=== Fixing POM file for Maven Central: $POM_FILE ==="
cp "$POM_FILE" "$POM_FILE.bak"

# Fix plugin versions using direct line replacement
PLUGIN_LINE=$(grep -n "central-publishing-maven-plugin" "$POM_FILE" | head -1 | cut -d ':' -f 1)
if [ -n "$PLUGIN_LINE" ]; then
  VERSION_LINE=$((PLUGIN_LINE + 1))
  echo "Found central-publishing-maven-plugin at line $PLUGIN_LINE, replacing version at line $VERSION_LINE"
  sed -i "${VERSION_LINE}s|.*|                <version>0.8.0</version>|" "$POM_FILE"
fi

GPG_LINE=$(grep -n "maven-gpg-plugin" "$POM_FILE" | head -1 | cut -d ':' -f 1)
if [ -n "$GPG_LINE" ]; then
  VERSION_LINE=$((GPG_LINE + 1))
  echo "Found maven-gpg-plugin at line $GPG_LINE, replacing version at line $VERSION_LINE"
  sed -i "${VERSION_LINE}s|.*|                <version>3.2.8</version>|" "$POM_FILE"
fi

# Fix name tags
echo "Fixing name tags..."
# First name tag (typically line 11)
NAME_LINE=$(grep -n "<n>PayPal Messages</n>" "$POM_FILE" | head -1 | cut -d ':' -f 1)
if [ -n "$NAME_LINE" ]; then
  echo "Fixing PayPal Messages name tag at line $NAME_LINE"
  sed -i "${NAME_LINE}s|.*|    <name>PayPal Messages</name>|" "$POM_FILE"
fi

# License name tag
LICENSE_LINE=$(grep -n "<n>The Apache License, Version 2.0</n>" "$POM_FILE" | head -1 | cut -d ':' -f 1)
if [ -n "$LICENSE_LINE" ]; then
  echo "Fixing Apache License name tag at line $LICENSE_LINE"
  sed -i "${LICENSE_LINE}s|.*|            <name>The Apache License, Version 2.0</name>|" "$POM_FILE"
fi

# Developer name tag
DEV_LINE=$(grep -n "<n>PayPalMessages Android</n>" "$POM_FILE" | head -1 | cut -d ':' -f 1)
if [ -n "$DEV_LINE" ]; then
  echo "Fixing PayPalMessages Android name tag at line $DEV_LINE"
  sed -i "${DEV_LINE}s|.*|            <name>PayPalMessages Android</name>|" "$POM_FILE"
fi

# Verify changes
echo "=== Verifying changes ==="
echo "- Central plugin version:"
grep -n "central-publishing-maven-plugin" -A 2 "$POM_FILE" || echo "Central plugin not found!"

# Check if central plugin has correct version
if grep -A 2 "central-publishing-maven-plugin" "$POM_FILE" | grep -q "0.8.0"; then
  echo "✅ Central plugin version correctly set to 0.8.0"
else
  echo "❌ Failed to set central plugin version to 0.8.0"
  echo "Creating completely new POM file..."
  
  # Get the version from the original POM
  VERSION=$(grep -o "<version>[^<]*</version>" "$POM_FILE" | head -1 | sed 's/<version>\(.*\)<\/version>/\1/')
  echo "Using version: $VERSION"
  
  # Create a completely new POM file
  cat > "$POM_FILE" << XML
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

    <build>
        <directory>\${project.basedir}/build</directory>
        <extensions>
            <!-- For AAR packaging support -->
            <extension>
                <groupId>org.apache.maven.wagon</groupId>
                <artifactId>wagon-http</artifactId>
                <version>3.5.3</version>
            </extension>
            <extension>
                <groupId>org.apache.maven.archetype</groupId>
                <artifactId>archetype-packaging</artifactId>
                <version>3.2.1</version>
            </extension>
            <extension>
                <groupId>com.android.tools.build</groupId>
                <artifactId>aar-maven-plugin</artifactId>
                <version>8.0.2</version>
            </extension>
            <extension>
                <groupId>org.sonatype.aether</groupId>
                <artifactId>aether-aar-uri-provider</artifactId>
                <version>1.13.1</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.sonatype.central</groupId>
                <artifactId>central-publishing-maven-plugin</artifactId>
                <version>0.8.0</version>
                <extensions>true</extensions>
                <configuration>
                    <publishingServerId>central</publishingServerId>
                    <tokenAuth>true</tokenAuth>
                    <autoPublish>true</autoPublish>
                    <waitUntil>validated</waitUntil>
                    <deploymentName>PayPal Messages Android \${project.version}</deploymentName>
                </configuration>
                <executions>
                    <execution>
                        <id>publish-to-central</id>
                        <phase>deploy</phase>
                        <goals>
                            <goal>publish</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-gpg-plugin</artifactId>
                <version>3.2.8</version>
                <executions>
                    <execution>
                        <id>sign-artifacts</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>sign</goal>
                        </goals>
                        <configuration>
                            <keyname>\${env.SIGNING_KEY_ID}</keyname>
                            <passphrase>\${env.SIGNING_KEY_PASSWORD}</passphrase>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    
    <dependencies>
        <!-- Regular JAR dependencies (compile scope) -->
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
        
        <!-- Android dependencies (provided scope) -->
        <dependency>
            <groupId>androidx.core</groupId>
            <artifactId>core-ktx</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>androidx.appcompat</groupId>
            <artifactId>appcompat</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.google.android.material</groupId>
            <artifactId>material</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>androidx.compose.foundation</groupId>
            <artifactId>foundation</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>androidx.compose.runtime</groupId>
            <artifactId>runtime</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>androidx.compose.ui</groupId>
            <artifactId>ui</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>androidx.compose.material3</groupId>
            <artifactId>material3</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>androidx.activity</groupId>
            <artifactId>activity-compose</artifactId>
            <version>1.1.7</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
XML
  
  echo "Created completely new POM file"
  grep -n "central-publishing-maven-plugin" -A 2 "$POM_FILE"
fi

echo "=== POM file fixed! ==="
