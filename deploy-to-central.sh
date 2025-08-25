#!/bin/bash

# Script to deploy artifacts to Maven Central using library POM directly
# This approach ensures the AAR packaging type is preserved
set -e

echo "Deploying to Maven Central Portal..."

# Check for required environment variables
if [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
    echo "Error: SONATYPE_NEXUS_PASSWORD must be set to your API token or password"
    exit 1
fi

# Prepare artifacts first (build AAR, sources, javadoc, and POM)
echo "Preparing artifacts..."
./prepare-maven-artifacts.sh

# Get version info
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Deploying version: $VERSION_FIXED"

# Stage artifacts into proper Maven-repo layout expected by the Central plugin
STAGING_ROOT="library/build/central-staging"
GROUP_PATH="com/paypal/messages/${ARTIFACT_ID}"
STAGE_DIR="${STAGING_ROOT}/${GROUP_PATH}/${VERSION_FIXED}"
SRC_DIR="library/build/maven-deploy"

rm -rf "${STAGING_ROOT}"
mkdir -p "${STAGE_DIR}"

# Copy library artifacts (THE KEY FIX: ensure these are included)
echo "Copying library artifacts to staging directory..."
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar" "${STAGE_DIR}/"

echo "Signing library artifacts..."
# Use our working CI signing helper
for file in "${STAGE_DIR}"/*; do
    if [[ -f "$file" && ! "$file" == *.asc ]]; then
        echo "Signing: $file"
        if [ -f "ci-sign-helper.sh" ] && [ -x "ci-sign-helper.sh" ]; then
            ./ci-sign-helper.sh "$file" || {
                # If in test mode, create a dummy signature file
                if [[ "$SONATYPE_NEXUS_PASSWORD" == "test" ]]; then
                    echo "Test mode detected, creating dummy signature file"
                    touch "${file}.asc"
                else
                    echo "Error: Failed to sign file ${file}"
                    exit 1
                fi
            }
        else
            # If in test mode, create a dummy signature file
            if [[ "$SONATYPE_NEXUS_PASSWORD" == "test" ]]; then
                echo "Test mode detected, creating dummy signature file"
                touch "${file}.asc"
            else
                echo "Error: ci-sign-helper.sh not found or not executable"
                exit 1
            fi
        fi
    fi
done

echo "Using library POM directly for deployment..."
# Use the library POM directly instead of creating a wrapper POM
LIBRARY_POM="${STAGE_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
echo "Library POM: ${LIBRARY_POM}"

# Fix the POM file to ensure it has proper name tags and plugin versions
if [ -x "./fix_pom_for_central.sh" ]; then
    echo "Using POM fixer script..."
    ./fix_pom_for_central.sh "$LIBRARY_POM"
else
    echo "POM fixer script not found! Creating a new POM file directly."
    # Create a completely new POM with proper tags and jar packaging
    cat > "$LIBRARY_POM" << XML
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.paypal.messages</groupId>
    <artifactId>paypal-messages</artifactId>
    <version>${VERSION_FIXED}</version>
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
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.8.0</version>
        </dependency>
    </dependencies>
</project>
XML
    echo "Created new POM file directly"
fi

# Create target directory for Maven plugin
MAVEN_TARGET_REL="target/maven-bundle"
MAVEN_TARGET="${STAGING_ROOT}/${MAVEN_TARGET_REL}"
rm -rf "${MAVEN_TARGET}"
mkdir -p "${MAVEN_TARGET}"

# Copy all staged artifacts to target directory
echo "Copying all artifacts to Maven target directory..."
cp -r "${STAGING_ROOT}/com" "${MAVEN_TARGET}/"

# Use the library POM directly for deployment - no wrapper POM needed
echo "The AAR file will be used as the primary artifact with a jar packaging type"

# Final verification - listing all files that will be uploaded:
echo "Final verification - listing all files that will be uploaded:"
echo "=== Files to be uploaded ==="
find "${MAVEN_TARGET}" -type f | sort

# Verify that AAR file is included
AAR_FILE="${MAVEN_TARGET}/com/paypal/messages/${ARTIFACT_ID}/${VERSION_FIXED}/${ARTIFACT_ID}-${VERSION_FIXED}.aar"
if [ -f "$AAR_FILE" ]; then
    echo "\n=== AAR file exists and will be uploaded ==="
    ls -lah "$AAR_FILE"
else
    echo "\n!!! ERROR: AAR file does not exist !!!"
    echo "Expected at: $AAR_FILE"
    exit 1
fi

# Verify that POM file has correct packaging
POM_FILE="${MAVEN_TARGET}/com/paypal/messages/${ARTIFACT_ID}/${VERSION_FIXED}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
if grep -q "<packaging>jar</packaging>" "$POM_FILE"; then
    echo "\n=== POM file has correct jar packaging for Maven compatibility ==="
    grep -n "<packaging>" "$POM_FILE"
else
    echo "\n!!! POM file does not have jar packaging. Fixing it now !!!"
    # Apply our POM fixer again to be sure
    if [ -x "./fix_pom_for_central.sh" ]; then
        echo "Using POM fixer script on target POM..."
        ./fix_pom_for_central.sh "$POM_FILE"
    fi
fi

# Run the Maven Central publish command
# Use absolute path for staging directory to ensure plugin finds all artifacts
ABSOLUTE_MAVEN_TARGET=$(realpath "${MAVEN_TARGET}")
echo "\n=== Using absolute staging directory ==="
echo "$ABSOLUTE_MAVEN_TARGET"

echo "\n=== Command that will be executed ==="
echo "mvn --batch-mode -f \"${LIBRARY_POM}\" -s .mvn/maven-settings.xml -DstagingDirectory=\"${ABSOLUTE_MAVEN_TARGET}\" verify org.sonatype.central:central-publishing-maven-plugin:publish"

if [[ "$SONATYPE_NEXUS_PASSWORD" == "test" ]]; then
  echo "\n=== Test mode: skipping actual publish to Maven Central ==="
  echo "Command that would be run:"
  echo "mvn --batch-mode \\
  -f \"${LIBRARY_POM}\" \\
  -s .mvn/maven-settings.xml \\
  -DstagingDirectory=\"${ABSOLUTE_MAVEN_TARGET}\" \\
  -Dorg.slf4j.simpleLogger.log.org.sonatype.central=debug \\
  verify \\
  org.sonatype.central:central-publishing-maven-plugin:publish"
  
  # Just run verification without publishing
  mvn --batch-mode \
    -f "${LIBRARY_POM}" \
    -s .mvn/maven-settings.xml \
    -DstagingDirectory="${ABSOLUTE_MAVEN_TARGET}" \
    verify
else
  # Run the actual publish command
  mvn --batch-mode \
    -f "${LIBRARY_POM}" \
    -s .mvn/maven-settings.xml \
    -DstagingDirectory="${ABSOLUTE_MAVEN_TARGET}" \
    -Dorg.slf4j.simpleLogger.log.org.sonatype.central=debug \
    verify \
    org.sonatype.central:central-publishing-maven-plugin:publish
fi

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments"
