#!/bin/bash
# Fix POM file for Maven Central - using jar packaging type for Maven compatibility
set -e

POM_FILE="$1"
if [ ! -f "$POM_FILE" ]; then
  echo "Usage: $0 <pom-file>"
  exit 1
fi

echo "=== Fixing POM file for Maven Central: $POM_FILE ==="
cp "$POM_FILE" "$POM_FILE.bak"

# Get the version from the original POM
VERSION=$(grep -o "<version>[^<]*</version>" "$POM_FILE" | head -1 | sed 's/<version>\(.*\)<\/version>/\1/')
echo "Using version: $VERSION"

# Create a completely new POM file optimized for Maven Central
cat > "$POM_FILE" << XML
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.paypal.messages</groupId>
    <artifactId>paypal-messages</artifactId>
    <version>${VERSION}</version>
    <packaging>jar</packaging>

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
            <!-- Extension mappings to handle AAR files -->
            <extension>
                <groupId>org.apache.maven.wagon</groupId>
                <artifactId>wagon-file</artifactId>
                <version>3.5.3</version>
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
                    <autoPublish>false</autoPublish>
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
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.8.0</version>
        </dependency>
    </dependencies>
    
    <!-- Define classifiers for non-JAR artifacts -->
    <distributionManagement>
        <relocation>
            <groupId>com.paypal.messages</groupId>
            <artifactId>paypal-messages</artifactId>
            <message>This artifact has been relocated to use the AAR format as the primary artifact</message>
        </relocation>
    </distributionManagement>
    
    <!-- Define file extension mappings -->
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <android.library>true</android.library>
        <aar.classifier>aar</aar.classifier>
    </properties>
</project>
XML

echo "Created new POM file with jar packaging type and minimal dependencies"

# Verify the new POM file
echo "=== Verifying POM file ==="
echo "- Packaging type:"
grep -n "<packaging>" "$POM_FILE" || echo "No packaging type found!"

echo "- Extension mappings:"
grep -n "<extension>" -A 4 "$POM_FILE" || echo "No extension mappings found!"

echo "- Plugin versions:"
grep -n "central-publishing-maven-plugin" -A 2 "$POM_FILE" || echo "Central plugin not found!"
grep -n "maven-gpg-plugin" -A 2 "$POM_FILE" || echo "GPG plugin not found!"

echo "- Name tags:"
grep -n "<name>" "$POM_FILE" | head -3 || echo "No name tags found!"

echo "- Dependencies:"
grep -n "<dependency>" -A 4 "$POM_FILE" || echo "No dependencies found!"

echo "- Classifiers and properties:"
grep -n "<aar.classifier>" "$POM_FILE" || echo "No AAR classifier found!"
grep -n "<android.library>" "$POM_FILE" || echo "No android.library property found!"

echo "=== POM file fixed! ==="
