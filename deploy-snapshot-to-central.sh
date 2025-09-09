#!/bin/bash

# Script to deploy artifacts to Maven Central using library POM directly (Snapshot or Release depending on version)
# This approach uses jar packaging type with extension mappings for AAR files
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
# Normalize potential double -SNAPSHOT (seen in logs)
VERSION_FIXED=$(echo "$VERSION" | sed 's/-SNAPSHOT-SNAPSHOT$/-SNAPSHOT/')
ARTIFACT_ID="paypal-messages"
GROUP_ID="com.paypal.messages"

echo "Deploying version: $VERSION_FIXED"

# Stage artifacts into proper Maven-repo layout expected by the Central plugin
STAGING_ROOT="library/build/central-staging"
GROUP_PATH="com/paypal/messages/${ARTIFACT_ID}"
STAGE_DIR="${STAGING_ROOT}/${GROUP_PATH}/${VERSION_FIXED}"
# Use libs output where prepare-maven-artifacts.sh places files
SRC_DIR="library/build/libs"

rm -rf "${STAGING_ROOT}"
mkdir -p "${STAGE_DIR}"

# Ensure expected source artifacts exist, build AAR if missing
if [ ! -f "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar" ]; then
  echo "AAR not found in ${SRC_DIR}, attempting to build :library:assembleRelease"
  ./gradlew :library:assembleRelease --no-daemon --stacktrace
  AAR_PATH="library/build/outputs/aar/library-release.aar"
  if [ -f "$AAR_PATH" ]; then
    cp "$AAR_PATH" "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar"
  else
    echo "Error: AAR still not found after build"; exit 1
  fi
fi

# Ensure POM exists (prepare-maven-artifacts.sh creates it)
if [ ! -f "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom" ]; then
  echo "Error: POM not found at ${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"; exit 1
fi

# Ensure sources and javadoc jars exist
if [ ! -f "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar" ]; then
  echo "Error: sources jar not found at ${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar"; exit 1
fi
if [ ! -f "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar" ]; then
  echo "Error: javadoc jar not found at ${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar"; exit 1
fi

# Copy library artifacts to staging directory
echo "Copying library artifacts to staging directory..."
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.aar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-sources.jar" "${STAGE_DIR}/"
cp "${SRC_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}-javadoc.jar" "${STAGE_DIR}/"

# Sign artifacts present in stage directory
echo "Signing library artifacts..."
for file in "${STAGE_DIR}"/*; do
    if [[ -f "$file" && ! "$file" == *.asc ]]; then
        echo "Signing: $file"
        if [ -f "ci-sign-helper.sh" ] && [ -x "ci-sign-helper.sh" ]; then
            ./ci-sign-helper.sh "$file" || {
                if [ "${ALLOW_FAKE_SIGNATURES:-false}" = "true" ]; then
                    echo "Creating dummy signature file (test-only) due to ALLOW_FAKE_SIGNATURES=true"
                    touch "${file}.asc"
                else
                    echo "Error: Failed to sign file ${file}"; exit 1
                fi
            }
        else
            if [ "${ALLOW_FAKE_SIGNATURES:-false}" = "true" ]; then
                echo "Creating dummy signature file (test-only) due to ALLOW_FAKE_SIGNATURES=true"
                touch "${file}.asc"
            else
                echo "Error: ci-sign-helper.sh not found or not executable"; exit 1
            fi
        fi
    fi
done

echo "Using library POM directly for deployment..."
LIBRARY_POM="${STAGE_DIR}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
echo "Library POM: ${LIBRARY_POM}"

# Re-create a POM that matches Central expectations
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
        <extensions>
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

# Create target directory for Maven plugin
MAVEN_TARGET_REL="target/maven-bundle"
MAVEN_TARGET="${STAGING_ROOT}/${MAVEN_TARGET_REL}"
rm -rf "${MAVEN_TARGET}"
mkdir -p "${MAVEN_TARGET}"

# Copy all staged artifacts to target directory
echo "Copying all artifacts to Maven target directory..."
cp -r "${STAGING_ROOT}/com" "${MAVEN_TARGET}/"

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

# Verify that POM file has correct packaging and extensions
POM_FILE="${MAVEN_TARGET}/com/paypal/messages/${ARTIFACT_ID}/${VERSION_FIXED}/${ARTIFACT_ID}-${VERSION_FIXED}.pom"
if grep -q "<packaging>jar</packaging>" "$POM_FILE"; then
    echo "\n=== POM file has correct jar packaging for Maven Central compatibility ==="
    grep -n "<packaging>" "$POM_FILE"
fi

# Use absolute path for staging directory to ensure plugin finds all artifacts
ABSOLUTE_MAVEN_TARGET=$(realpath "${MAVEN_TARGET}")
echo "\n=== Using absolute staging directory ==="
echo "$ABSOLUTE_MAVEN_TARGET"

echo "\n=== Command that will be executed ==="
echo "mvn --batch-mode -f \"${LIBRARY_POM}\" -s .mvn/maven-settings.xml -DstagingDirectory=\"${ABSOLUTE_MAVEN_TARGET}\" verify org.sonatype.central:central-publishing-maven-plugin:publish"

# Run the actual publish command
mvn --batch-mode \
  -f "${LIBRARY_POM}" \
  -s .mvn/maven-settings.xml \
  -DstagingDirectory="${ABSOLUTE_MAVEN_TARGET}" \
  -Dorg.slf4j.simpleLogger.log.org.sonatype.central=debug \
  verify \
  org.sonatype.central:central-publishing-maven-plugin:publish

echo "Deployment initiated successfully!"
echo "Check the status at: https://central.sonatype.com/publishing/deployments" 