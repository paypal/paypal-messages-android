#!/bin/bash
# Fix all Maven Central deployment issues at once
set -e

echo "==== PayPal Messages Android SDK Maven Central Fix Script ===="

# Step 1: Fix name tags in POM files
echo "Step 1: Fixing name tags in POM files..."

# Fix library POM
if [ -f "library/pom.xml" ]; then
  bash fix_name_in_pom.sh library/pom.xml
else
  echo "WARNING: library/pom.xml not found"
fi

# Step 2: Create proper root POM
echo "Step 2: Creating proper root POM..."
bash fix_pom_in_action_mac.sh

# Step 3: Fix GitHub Action YML if needed
if [ -f ".github/actions/publish_maven_central/action.yml" ]; then
  echo "Step 3: Fixing GitHub Action YML..."
  bash fix_action_yml.sh
else
  echo "Step 3: GitHub Action YML not found, skipping"
fi

# Step 4: Create signatures for POM files
echo "Step 4: Creating signatures for POM files..."
bash fix_pom_signatures.sh pom.xml library/pom.xml

# Step 5: Verify all requirements are met
echo "Step 5: Verifying Maven Central requirements..."
bash verify_maven_central.sh

echo "==== Maven Central fix script completed ===="
