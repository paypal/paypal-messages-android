#!/bin/bash

# Script to deploy artifacts to Maven Central using the Central Portal Maven Plugin
set -e

echo "Deploying to Maven Central Portal..."

# Check for required environment variables
if [ -z "$SONATYPE_NEXUS_USERNAME" ] || [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_USERNAME and SONATYPE_NEXUS_PASSWORD must be set"
    exit 1
fi

# Prepare artifacts first
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
ARTIFACT_ID="paypal-messages"

echo "Deploying version: $VERSION"

# Change to the maven-deploy directory
cd library/build/maven-deploy

# The pom.xml already has the central-publishing-maven-plugin configured
# We just need to attach the artifacts and deploy

# Create a temporary pom with file references
cat > deploy-pom.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.paypal.messages</groupId>
    <artifactId>${ARTIFACT_ID}</artifactId>
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
                    <artifact>\${project.basedir}/${ARTIFACT_ID}-${VERSION}.aar</artifact>
                    <sources>\${project.basedir}/${ARTIFACT_ID}-${VERSION}-sources.jar</sources>
                </configuration>
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
            <version>1.10.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>androidx.appcompat</groupId>
            <artifactId>appcompat</artifactId>
            <version>1.6.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.google.android.material</groupId>
            <artifactId>material</artifactId>
            <version>1.9.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
EOF

# Use Maven to deploy with the Central Portal plugin
echo "Running Maven deploy..."
mvn deploy \
  -f deploy-pom.xml \
  -s ../../../.mvn/maven-settings.xml \
  -Dfile=${ARTIFACT_ID}-${VERSION}.aar \
  -Dsources=${ARTIFACT_ID}-${VERSION}-sources.jar \
  -DgeneratePom=false \
  -DpomFile=${ARTIFACT_ID}-${VERSION}.pom

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"