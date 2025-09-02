#!/bin/bash
# Script to fix all name tags in all relevant files
set -e

# Find all POM files
echo "Finding all POM files..."
POM_FILES=$(find . -name "*.pom" -o -name "pom.xml")

# Fix each POM file
for POM_FILE in $POM_FILES; do
  echo "Fixing name tags in $POM_FILE..."
  
  # Create backup
  cp "$POM_FILE" "${POM_FILE}.bak"
  
  # Fix with direct string replacement using Unix tools that actually work
  sed -i.tmp 's/<n>/<name>/g' "$POM_FILE"
  sed -i.tmp 's/<\/n>/<\/name>/g' "$POM_FILE"
  
  # Clean up temporary files
  rm -f "${POM_FILE}.tmp" 2>/dev/null || true
  
  # Verify fix
  if grep -q "<n>" "$POM_FILE" || grep -q "</n>" "$POM_FILE"; then
    echo "WARNING: Name tags still exist in $POM_FILE"
    
    # Try with perl as fallback
    perl -i -pe 's/<n>/<name>/g' "$POM_FILE"
    perl -i -pe 's/<\/n>/<\/name>/g' "$POM_FILE"
    
    # Check again
    if grep -q "<n>" "$POM_FILE" || grep -q "</n>" "$POM_FILE"; then
      echo "ERROR: Could not fix name tags in $POM_FILE"
      echo "Manual inspection required"
    else
      echo "✓ Fixed with perl fallback"
    fi
  else
    echo "✓ Fixed successfully"
  fi
done

echo "All POM files processed"
