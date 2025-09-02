#!/bin/bash
# Script to fix name tags using file redirection
set -e

echo "===== Fixing name tags in all POM files ====="

# Function to fix POM file
fix_pom() {
  local pom_file="$1"
  echo "Processing: $pom_file"
  
  # Create a backup
  cp "$pom_file" "${pom_file}.bak"
  
  # Create a temporary file
  temp_file="${pom_file}.tmp"
  
  # Process file line by line to avoid sed/perl issues
  while IFS= read -r line; do
    # Replace closing tag first to avoid issues
    line="${line//\<\/n\>/\<\/name\>}"
    # Then replace opening tag
    line="${line//\<n\>/\<name\>}"
    echo "$line" >> "$temp_file"
  done < "$pom_file"
  
  # Replace original with fixed file
  mv "$temp_file" "$pom_file"
  
  # Verify
  if grep -q "<n>" "$pom_file" || grep -q "</n>" "$pom_file"; then
    echo "❌ Failed to fix tags in $pom_file"
    return 1
  else
    echo "✓ Successfully fixed tags in $pom_file"
    return 0
  fi
}

# Find all POM files
echo "Finding all POM files..."
pom_files=$(find . -name "*.pom" -o -name "pom.xml")

# Process each file
for pom_file in $pom_files; do
  fix_pom "$pom_file"
done

echo "===== All POM files processed ====="
