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

# Create a temporary pom that attaches our AAR and sources, signs all artifacts, then publishes via Central
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
            <!-- Attach prepared AAR and sources to this POM so they are included in the Central bundle -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>3.4.0</version>
                <executions>
                    <execution>
                        <id>attach-aar-and-sources</id>
                        <phase>package</phase>
                        <goals>
                            <goal>attach-artifact</goal>
                        </goals>
                        <configuration>
                            <artifacts>
                                <artifact>
                                    <file>\${project.basedir}/${ARTIFACT_ID}-${VERSION}.aar</file>
                                    <type>aar</type>
                                </artifact>
                                <artifact>
                                    <file>\${project.basedir}/${ARTIFACT_ID}-${VERSION}-sources.jar</file>
                                    <type>jar</type>
                                    <classifier>sources</classifier>
                                </artifact>
                            </artifacts>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- Sign the POM and all attached artifacts -->
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
                            <gpgArguments>
                                <arg>--pinentry-mode</arg>
                                <arg>loopback</arg>
                            </gpgArguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- Central Publishing plugin -->
            <plugin>
                <groupId>org.sonatype.central</groupId>
                <artifactId>central-publishing-maven-plugin</artifactId>
                <version>0.8.0</version>
                <extensions>true</extensions>
                <configuration>
                    <publishingServerId>central</publishingServerId>
                    <autoPublish>true</autoPublish>
                    <waitUntil>validated</waitUntil>
                    <deploymentName>PayPal Messages Android \${project.version}</deploymentName>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# Verify (to attach artifacts and generate .asc signatures), then publish via Central plugin
# Important: run signing and publishing in the same Maven invocation so .asc files are included
echo "Signing via Maven (verify) and publishing in a single run..."
mvn --batch-mode \
  -f deploy-pom.xml \
  -s ../../../.mvn/maven-settings.xml \
  -DskipTests verify org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"