#!/bin/bash
# This script verifies the POM structure and packaging type before publishing

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Verifying POM structure for Maven Central publishing...${NC}"

# Step 1: Run a local publish to Maven local to verify the POM structure
echo -e "\n${YELLOW}Running publishToMavenLocal to generate artifacts...${NC}"
./gradlew clean publishToMavenLocal -xtest

# Find the latest version
VERSION=$(grep "sdkVersionName" build.gradle | sed -E 's/.*"([^"]+)".*/\1/')
echo -e "\n${YELLOW}Checking POM for version: ${VERSION}${NC}"

# Check the local Maven repository for the POM file
USER_HOME=$(eval echo ~$USER)
POM_PATH="${USER_HOME}/.m2/repository/com/paypal/messages/paypal-messages/${VERSION}/paypal-messages-${VERSION}.pom"

if [ ! -f "$POM_PATH" ]; then
    echo -e "${RED}ERROR: POM file not found at ${POM_PATH}${NC}"
    exit 1
fi

echo -e "${GREEN}Found POM file at: ${POM_PATH}${NC}"

# Check packaging type
PACKAGING=$(grep -o "<packaging>.*</packaging>" "$POM_PATH" | sed -E 's/<packaging>(.*)<\/packaging>/\1/')

if [ "$PACKAGING" != "aar" ]; then
    echo -e "${RED}ERROR: Packaging type is not set to 'aar'. Found: ${PACKAGING}${NC}"
    echo -e "${YELLOW}Please check gradle-publish.gradle and ensure packaging is set to 'aar'.${NC}"
    exit 1
else
    echo -e "${GREEN}✓ Packaging type correctly set to 'aar'${NC}"
fi

# Check if dependencies section exists
if grep -q "<dependencies>" "$POM_PATH"; then
    echo -e "${GREEN}✓ Dependencies section exists in POM${NC}"
    
    # Count dependencies
    DEP_COUNT=$(grep -c "<dependency>" "$POM_PATH")
    echo -e "${GREEN}  Found ${DEP_COUNT} dependencies declared in POM${NC}"
else
    echo -e "${RED}ERROR: No dependencies section found in POM.${NC}"
    echo -e "${YELLOW}Please check gradle-publish.gradle withXml section.${NC}"
    exit 1
fi

# Verify the AAR file exists
AAR_PATH="${USER_HOME}/.m2/repository/com/paypal/messages/paypal-messages/${VERSION}/paypal-messages-${VERSION}.aar"
if [ -f "$AAR_PATH" ]; then
    echo -e "${GREEN}✓ AAR file exists at: ${AAR_PATH}${NC}"
    
    # Get file size
    AAR_SIZE=$(du -h "$AAR_PATH" | cut -f1)
    echo -e "${GREEN}  AAR file size: ${AAR_SIZE}${NC}"
else
    echo -e "${RED}ERROR: AAR file not found at ${AAR_PATH}${NC}"
    exit 1
fi

# Check sources JAR
SOURCES_PATH="${USER_HOME}/.m2/repository/com/paypal/messages/paypal-messages/${VERSION}/paypal-messages-${VERSION}-sources.jar"
if [ -f "$SOURCES_PATH" ]; then
    echo -e "${GREEN}✓ Sources JAR exists at: ${SOURCES_PATH}${NC}"
else
    echo -e "${RED}ERROR: Sources JAR not found at ${SOURCES_PATH}${NC}"
    echo -e "${YELLOW}Please check that 'withSourcesJar()' is correctly configured.${NC}"
    exit 1
fi

echo -e "\n${YELLOW}Displaying POM contents for review:${NC}"
echo "----------------------------------------"
cat "$POM_PATH"
echo "----------------------------------------"

echo -e "\n${GREEN}✓ POM verification complete. Structure appears correct.${NC}"
echo -e "${YELLOW}Ready to publish to Maven Central.${NC}"

# Final notes
echo -e "\n${YELLOW}IMPORTANT NOTES:${NC}"
echo -e "1. Make sure you have set up the signing keys and Sonatype credentials."
echo -e "2. Run the following command to publish to Maven Central:"
echo -e "   ${YELLOW}./gradlew publishReleasePublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository${NC}"
echo -e "3. After publishing, verify the artifacts are correctly available on Maven Central (may take some time to sync)."