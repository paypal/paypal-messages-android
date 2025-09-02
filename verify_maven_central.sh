#!/bin/bash
# Script to verify Maven Central deployment requirements
set -e

# Default POM files to check
if [ $# -eq 0 ]; then
  POM_FILES="pom.xml library/pom.xml"
else
  POM_FILES="$@"
fi

echo "=== Verifying Maven Central deployment requirements ==="

missing_items=0

for POM_FILE in $POM_FILES; do
  if [ ! -f "$POM_FILE" ]; then
    echo "WARNING: POM file not found: $POM_FILE"
    continue
  fi
  
  echo "Checking $POM_FILE..."
  
  # Check for POM signature
  if [ ! -f "${POM_FILE}.asc" ]; then
    echo "❌ Missing signature file: ${POM_FILE}.asc"
    missing_items=$((missing_items + 1))
  else
    echo "✓ Found signature file: ${POM_FILE}.asc"
  fi
  
  # Check for name tags
  if grep -q "<n>" "$POM_FILE" || grep -q "</n>" "$POM_FILE"; then
    echo "❌ Found <n> tags that need to be replaced with <name> tags"
    missing_items=$((missing_items + 1))
  else
    echo "✓ No <n> tags found"
  fi
  
  # Check for project name
  if ! grep -q "<name>" "$POM_FILE"; then
    echo "❌ Missing project name"
    missing_items=$((missing_items + 1))
  else
    echo "✓ Found project name"
  fi
  
  # Check for description
  if ! grep -q "<description>" "$POM_FILE"; then
    echo "❌ Missing project description"
    missing_items=$((missing_items + 1))
  else
    echo "✓ Found project description"
  fi
  
  # Check for URL
  if ! grep -q "<url>" "$POM_FILE"; then
    echo "❌ Missing project URL"
    missing_items=$((missing_items + 1))
  else
    echo "✓ Found project URL"
  fi
  
  # Check for licenses
  if ! grep -q "<licenses>" "$POM_FILE"; then
    echo "❌ Missing license information"
    missing_items=$((missing_items + 1))
  else
    echo "✓ Found license information"
  fi
  
  # Check for SCM
  if ! grep -q "<scm>" "$POM_FILE"; then
    echo "❌ Missing SCM information"
    missing_items=$((missing_items + 1))
  else
    echo "✓ Found SCM information"
  fi
  
  # Check for developers
  if ! grep -q "<developers>" "$POM_FILE"; then
    echo "❌ Missing developers information"
    missing_items=$((missing_items + 1))
  else
    echo "✓ Found developers information"
  fi
  
  echo ""
done

if [ $missing_items -eq 0 ]; then
  echo "✅ All Maven Central requirements are met!"
  exit 0
else
  echo "❌ Found $missing_items issues that need to be fixed for Maven Central deployment"
  exit 1
fi
