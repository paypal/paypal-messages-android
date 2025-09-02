#!/bin/bash
# Script to create or fix the root POM for Maven Central deployment
set -e

# Check if the POM file exists
if [ -f "pom.xml" ]; then
  echo "Fixing existing root pom.xml for Maven Central"
  cp pom.xml pom.xml.bak
else
  echo "Creating new root pom.xml for Maven Central"
fi

# Get the version from the library POM or build.gradle
if [ -f "library/pom.xml" ]; then
  VERSION=$(grep -o "<version>[^<]*</version>" "library/pom.xml" | head -1 | sed "s/<version>\(.*\)<\/version>/\1/")
else
  VERSION=$(grep -o "\"sdkVersionName\"\s*:\s*\"[^\"]*\"" build.gradle | grep -o "\"[^\"]*\"\$" | tr -d "\"")
fi
echo "Using version: $VERSION"

# Create a proper POM file for Maven Central
cat > pom.xml << EOF
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
    
    <build>
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
                    <deploymentName>PayPal Messages Android ${project.version}</deploymentName>
                </configuration>
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
</project>
EOF

echo "Root pom.xml created/updated successfully for Maven Central deployment"
