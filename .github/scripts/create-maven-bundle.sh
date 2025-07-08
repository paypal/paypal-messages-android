#!/bin/bash
# Script to create a Maven Central-compatible bundle for the Central Portal API

set -e  # Exit on error

# Default variables
OUTPUT_DIR="bundle"
OUTPUT_ZIP="bundle.zip"

# Parse command line arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --output-dir) OUTPUT_DIR="$2"; shift ;;
        --output-zip) OUTPUT_ZIP="$2"; shift ;;
        --version) VERSION="$2"; shift ;;
        --group-id) GROUP_ID="$2"; shift ;;
        --artifact-id) ARTIFACT_ID="$2"; shift ;;
        *) echo "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

# Validate required parameters
if [ -z "$VERSION" ] || [ -z "$GROUP_ID" ] || [ -z "$ARTIFACT_ID" ]; then
    echo "Missing required parameters. Usage:"
    echo "./create-maven-bundle.sh --version VERSION --group-id GROUP_ID --artifact-id ARTIFACT_ID [--output-dir DIR] [--output-zip ZIP]"
    exit 1
fi

echo "Creating Maven bundle for $GROUP_ID:$ARTIFACT_ID:$VERSION"

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Define paths based on Maven standards
GROUP_PATH=$(echo "$GROUP_ID" | tr '.' '/')
ARTIFACT_PATH="$OUTPUT_DIR/$GROUP_PATH/$ARTIFACT_ID/$VERSION"
mkdir -p "$ARTIFACT_PATH"

# Function to find and copy artifacts
copy_artifacts() {
    local file_pattern="$1"
    local target_dir="$2"
    local found=false
    
    find . -name "$file_pattern" | while read -r file; do
        echo "Found artifact: $file"
        cp "$file" "$target_dir/"
        found=true
    done
    
    if [ "$found" = false ]; then
        echo "Warning: No artifacts found matching pattern $file_pattern"
    fi
}

# Copy the main artifacts
echo "Copying AAR files..."
copy_artifacts "$ARTIFACT_ID-$VERSION.aar" "$ARTIFACT_PATH"

echo "Copying JAR files..."
copy_artifacts "$ARTIFACT_ID-$VERSION.jar" "$ARTIFACT_PATH"
copy_artifacts "$ARTIFACT_ID-$VERSION-sources.jar" "$ARTIFACT_PATH"
copy_artifacts "$ARTIFACT_ID-$VERSION-javadoc.jar" "$ARTIFACT_PATH"

echo "Copying POM files..."
copy_artifacts "$ARTIFACT_ID-$VERSION.pom" "$ARTIFACT_PATH"

echo "Copying signature files..."
copy_artifacts "$ARTIFACT_ID-$VERSION*.asc" "$ARTIFACT_PATH"

# Create a zip file of the bundle
echo "Creating zip bundle at $OUTPUT_ZIP"
(cd "$OUTPUT_DIR" && zip -r "../$OUTPUT_ZIP" .)

echo "Bundle created successfully"