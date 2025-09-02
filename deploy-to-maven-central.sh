#!/bin/bash
# Script to deploy PayPal Messages Android SDK to Maven Central
set -e

# Parse command line arguments
AUTO_PUBLISH=false
while [[ "$#" -gt 0 ]]; do
  case $1 in
    --auto-publish) AUTO_PUBLISH=true ;;
    --no-auto-publish) AUTO_PUBLISH=false ;;
    *) echo "Unknown parameter: $1"; exit 1 ;;
  esac
  shift
done

echo "Deploying PayPal Messages Android SDK to Maven Central..."
echo "Auto-publish: $AUTO_PUBLISH"

# Import GPG key early so subsequent signing steps succeed
if [ -n "$SIGNING_KEY_FILE" ] && [ -f "$SIGNING_KEY_FILE" ]; then
  echo "Importing GPG key..."
  gpg --batch --import "$SIGNING_KEY_FILE"
  
  # Test GPG signing capability early
  echo "Testing GPG signing capability..."
  echo "test" | gpg --batch --yes --pinentry-mode loopback \
    --passphrase "${SIGNING_KEY_PASSWORD}" \
    --local-user "${SIGNING_KEY_ID}" \
    --armor --detach-sign --output /tmp/test.asc || echo "Warning: GPG test signing failed"
else
  echo "Warning: SIGNING_KEY_FILE not set or not found; GPG signing may fail."
fi

# Build and prepare the library
echo "Building library..."
./gradlew clean :library:assembleRelease

# Verify the AAR was created
AAR_FILE="library/build/outputs/aar/library-release.aar"
if [ ! -f "$AAR_FILE" ]; then
  echo "Error: AAR file not found at $AAR_FILE"
  exit 1
fi

# Create sources JAR
echo "Creating sources JAR..."
SOURCES_DIR="library/build/tmp/sources"
rm -rf "$SOURCES_DIR"
mkdir -p "$SOURCES_DIR"
cp -r library/src/main/java "$SOURCES_DIR/" 2>/dev/null || true
cp -r library/src/main/kotlin "$SOURCES_DIR/" 2>/dev/null || true

# Make sure the target directory exists
mkdir -p "library/build/libs"

SOURCES_JAR="library/build/libs/paypal-messages-sources.jar"
jar cf "$SOURCES_JAR" -C "$SOURCES_DIR" .

# Create minimal javadoc JAR (placeholder for Maven Central)
echo "Creating javadoc JAR..."
JAVADOC_DIR="library/build/tmp/javadoc"
rm -rf "$JAVADOC_DIR"
mkdir -p "$JAVADOC_DIR"
cat > "$JAVADOC_DIR/README.md" << EOF
# PayPal Messages Android SDK

This is a placeholder Javadoc JAR for the PayPal Messages Android SDK.
For detailed documentation, please visit: https://github.com/paypal/paypal-messages-android
EOF

# Make sure the target directory exists (redundant but for safety)
mkdir -p "library/build/libs"

JAVADOC_JAR="library/build/libs/paypal-messages-javadoc.jar"
jar cf "$JAVADOC_JAR" -C "$JAVADOC_DIR" .

# Create a properly formatted POM file
VERSION=$(grep -o '"sdkVersionName"\s*:\s*"[^"]*"' build.gradle | grep -o '"[^"]*"$' | tr -d '"')
echo "Detected version: $VERSION"

echo "Creating properly formatted POM file..."
# Use the prepare-maven-artifacts.sh script to create properly formatted POMs with all required metadata
./prepare-maven-artifacts.sh

# The POM file is now created at library/build/libs/paypal-messages.pom
POM_FILE="library/build/libs/paypal-messages.pom"

# Rename the AAR file to match our artifact ID
echo "Preparing files for deployment..."
AAR_DEST="library/build/libs/paypal-messages-${VERSION}.aar"
cp "$AAR_FILE" "$AAR_DEST"
mv "$SOURCES_JAR" "library/build/libs/paypal-messages-${VERSION}-sources.jar"
mv "$JAVADOC_JAR" "library/build/libs/paypal-messages-${VERSION}-javadoc.jar"
mv "$POM_FILE" "library/build/libs/paypal-messages-${VERSION}.pom"

# List the prepared files
echo "Prepared files for deployment:"
ls -la library/build/libs/

# Sign the artifacts directly
if [ -n "$SIGNING_KEY_ID" ] && [ -n "$SIGNING_KEY_PASSWORD" ]; then
  echo "Directly signing artifacts with GPG..."
  for file in library/build/libs/paypal-messages-${VERSION}*; do
    if [[ -f "$file" && ! "$file" == *.asc ]]; then
      echo "Signing: $file"
      gpg --batch --yes --pinentry-mode loopback --passphrase "${SIGNING_KEY_PASSWORD}" \
          --local-user "${SIGNING_KEY_ID}" --armor --detach-sign \
          --output "${file}.asc" "$file" || echo "Warning: Failed to sign $file"
    fi
  done
fi

# Deploy to Maven Central using Maven
echo "Deploying to Maven Central..."
echo "This step requires Maven and proper Sonatype credentials in .mvn/maven-settings.xml"

# Create .mvn directory if it doesn't exist
mkdir -p .mvn

# Create maven-settings.xml with proper credentials
cat > .mvn/maven-settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>central</id>
            <username>${env.SONATYPE_NEXUS_USERNAME}</username>
            <password>${env.SONATYPE_NEXUS_PASSWORD}</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>gpg</id>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.keyname>${env.SIGNING_KEY_ID}</gpg.keyname>
                <gpg.passphrase>${env.SIGNING_KEY_PASSWORD}</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>gpg</activeProfile>
    </activeProfiles>
</settings>
EOF

# Check if credentials are available
if [ -z "$SONATYPE_NEXUS_USERNAME" ] || [ -z "$SONATYPE_NEXUS_PASSWORD" ]; then
  echo "Warning: SONATYPE_NEXUS_USERNAME and/or SONATYPE_NEXUS_PASSWORD environment variables not set."
  echo "The deployment command will likely fail without these credentials."
fi

if [ -z "$SIGNING_KEY_ID" ] || [ -z "$SIGNING_KEY_PASSWORD" ] || [ -z "$SIGNING_KEY_FILE" ]; then
  echo "Warning: Signing credentials (SIGNING_KEY_ID, SIGNING_KEY_PASSWORD, SIGNING_KEY_FILE) not set."
  echo "The deployment command will likely fail without these credentials."
fi

# Deploy using Maven (requires Maven to be installed)
if command -v mvn &> /dev/null; then
  echo "Deploying to Maven Central using Maven..."
  
  # Deploy with the Central portal plugin
  # Root pom.xml was already created by prepare-maven-artifacts.sh
  echo "Using pre-created root pom.xml for Maven..."

  # Run the Maven command with the temporary POM
  echo "Attempting to publish with Maven from root directory..."
  mvn org.sonatype.central:central-publishing-maven-plugin:publish \
    -s .mvn/maven-settings.xml \
    -DrepositoryDir="library/build/libs" \
    -DautoPublish=$AUTO_PUBLISH \
    -Dverbose=true
  
  # If the above command fails, try running directly from the libs directory
  if [ $? -ne 0 ]; then
    echo "First attempt failed, trying to run Maven from the libs directory..."
    cd library/build/libs
    
    # Copy the pre-created root pom.xml to the libs directory
    cp ../../../pom.xml pom.xml
    
    # Copy the Maven settings file
    mkdir -p .mvn
    cp ../../../.mvn/maven-settings.xml .mvn/
    
    # Run Maven from the libs directory
    mvn org.sonatype.central:central-publishing-maven-plugin:publish \
      -s .mvn/maven-settings.xml \
      -DrepositoryDir="." \
      -DautoPublish=$AUTO_PUBLISH \
      -Dverbose=true
      
    # Return to the original directory
    cd ../../..
  fi
    
  # Clean up temporary pom.xml files
  rm -f pom.xml
  rm -f library/build/libs/pom.xml
  
  echo "Deployment submitted to Maven Central."
  echo "Check the status at: https://central.sonatype.com/publishing/deployments"
else
  echo "Maven not found. Please install Maven to deploy."
  exit 1
fi

echo "Deployment process completed."