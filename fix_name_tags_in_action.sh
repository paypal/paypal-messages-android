#!/bin/bash
# Fix <n> tags in GitHub Action YAML files
set -e

ACTION_YML="$1"
if [ ! -f "$ACTION_YML" ]; then
  ACTION_YML=".github/actions/publish_maven_central/action.yml"
  if [ ! -f "$ACTION_YML" ]; then
    echo "Usage: $0 <action-yml-file>"
    exit 1
  fi
fi

echo "Fixing <n> tags in $ACTION_YML"

# Create a backup of the original file
cp "$ACTION_YML" "$ACTION_YML.orig"

# Use perl to safely replace <n> tags with <name> tags
perl -i -pe 's/<n>PayPal Messages<\/n>/<name>PayPal Messages<\/name>/g' "$ACTION_YML"

# Verify the fix
echo "Verifying fix..."
if grep -q "<n>" "$ACTION_YML"; then
  echo "WARNING: There are still <n> tags in the action.yml file!"
  grep -n "<n>" "$ACTION_YML"
else
  echo "All <n> tags fixed successfully!"
fi

echo "Action file preparation complete."