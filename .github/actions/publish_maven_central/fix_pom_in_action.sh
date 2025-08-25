#!/bin/bash
# Fix POM file for Maven Central with a template-based approach
set -e

POM_FILE="$1"
VERSION="$2"
if [ ! -f "$POM_FILE" ] || [ -z "$VERSION" ]; then
  echo "Usage: $0 <pom-file> <version>"
  exit 1
fi

echo "Creating a completely new POM file with proper tags for version $VERSION"

# Always make a backup of the original file
cp "$POM_FILE" "$POM_FILE.bak"

# First try to fix the existing file directly
echo "Attempting direct fixes to POM file first..."
perl -i -pe 's|(<artifactId>central-publishing-maven-plugin</artifactId>\s*)<version>[^<]+</version>|\1<version>0.8.0</version>|g' "$POM_FILE"
perl -i -pe 's|(<artifactId>maven-gpg-plugin</artifactId>\s*)<version>[^<]+</version>|\1<version>3.2.8</version>|g' "$POM_FILE"
perl -i -pe 's|(<groupId>com\.google\.code\.gson</groupId>\s*<artifactId>gson</artifactId>\s*)<version>[^<]+</version>|\1<version>2.9.1</version>|g' "$POM_FILE"
perl -i -pe 's|(<groupId>com\.squareup\.okhttp3</groupId>\s*<artifactId>okhttp</artifactId>\s*)<version>[^<]+</version>|\1<version>4.8.0</version>|g' "$POM_FILE"
perl -i -pe 's|<n>PayPal Messages</n>|<name>PayPal Messages</name>|g' "$POM_FILE"
perl -i -pe 's|<n>The Apache License, Version 2.0</n>|<name>The Apache License, Version 2.0</name>|g' "$POM_FILE"
perl -i -pe 's|<n>PayPalMessages Android</n>|<name>PayPalMessages Android</name>|g' "$POM_FILE"

# Verify if direct fixes worked
if grep -q "<version>0.8.0</version>" "$POM_FILE" && grep -q "<name>" "$POM_FILE"; then
    echo "Direct fixes successful!"
    grep -n "central-publishing-maven-plugin" -A 2 "$POM_FILE"
    grep -n "<name>" "$POM_FILE" | head -3
    echo "Skipping template-based replacement."
    exit 0
fi

# If direct fixes failed, fall back to template replacement
echo "Direct fixes failed or incomplete. Creating new POM from template..."

# Create a new POM file from template
cat > "$POM_FILE.new" << XML
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.paypal.messages</groupId>
    <artifactId>paypal-messages</artifactId>
    <version>$VERSION</version>
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

# Move the new POM file into place
cp "$POM_FILE" "$POM_FILE.bak"
mv "$POM_FILE.new" "$POM_FILE"

echo "POM file replaced with properly formatted version: $POM_FILE"
echo "Verifying:"
echo "- Name tags:"
grep -n "<name>" "$POM_FILE" | head -3 || echo "No name tags found!"
echo "- Plugin versions:"
grep -n "central-publishing-maven-plugin" -A 2 "$POM_FILE" || echo "Plugin not found!"
echo "- Packaging type:"
grep -n "<packaging>aar</packaging>" "$POM_FILE" || echo "No AAR packaging found!"
