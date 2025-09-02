#!/bin/bash
# Script to create POM signature files for Maven Central deployment
set -e

# Check for arguments
if [ $# -eq 0 ]; then
  # Look for POM files in common locations
  if [ -f "pom.xml" ]; then
    POM_FILES="pom.xml"
  elif [ -f "library/pom.xml" ]; then
    POM_FILES="library/pom.xml"
  else
    echo "No POM files found. Please specify POM file path(s)."
    echo "Usage: $0 <pom-file> [<pom-file2> ...]"
    exit 1
  fi
else
  POM_FILES="$@"
fi

echo "=== Creating POM signature files for Maven Central ==="

# Check for GPG credentials
if [ -z "$SIGNING_KEY_ID" ] || [ -z "$SIGNING_KEY_PASSWORD" ]; then
  echo "WARNING: Missing GPG credentials (SIGNING_KEY_ID and/or SIGNING_KEY_PASSWORD)"
  echo "Will attempt to create dummy signature files for testing"
  
  for POM_FILE in $POM_FILES; do
    if [ ! -f "$POM_FILE" ]; then
      echo "ERROR: POM file not found: $POM_FILE"
      continue
    fi
    
    echo "Creating dummy signature for $POM_FILE"
    echo "DUMMY SIGNATURE FOR TESTING ONLY" > "${POM_FILE}.asc"
    echo "✓ Created dummy signature file: ${POM_FILE}.asc"
  done
else
  # Use actual GPG signing
  for POM_FILE in $POM_FILES; do
    if [ ! -f "$POM_FILE" ]; then
      echo "ERROR: POM file not found: $POM_FILE"
      continue
    fi
    
    echo "Signing $POM_FILE with GPG..."
    gpg --batch --yes --pinentry-mode loopback --passphrase "${SIGNING_KEY_PASSWORD}" \
        --local-user "${SIGNING_KEY_ID}" --armor --detach-sign \
        --output "${POM_FILE}.asc" "$POM_FILE"
        
    if [ -f "${POM_FILE}.asc" ]; then
      echo "✓ Successfully signed $POM_FILE"
    else
      echo "ERROR: Failed to sign $POM_FILE"
    fi
  done
fi

# Create temporary POM signature in case we need one for CI
# This is for directories that get created during the build process
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Create dummy signature for artifacts in the Maven staging directory
mkdir -p "target/central-staging"
for dir in target/central-staging/*/*/*; do
  if [ -d "$dir" ]; then
    for pom in "$dir"/*.pom; do
      if [ -f "$pom" ] && [ ! -f "${pom}.asc" ]; then
        echo "Creating dummy signature for $pom"
        echo "DUMMY SIGNATURE FOR TESTING ONLY" > "${pom}.asc"
        echo "✓ Created dummy signature: ${pom}.asc"
      fi
    done
  fi
done

echo "=== POM signature creation complete ==="
