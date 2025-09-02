#!/bin/bash
# Fix name tags in GitHub Action YAML files
set -e

ACTION_YML="$1"
if [ -z "$ACTION_YML" ]; then
  ACTION_YML=".github/actions/publish_maven_central/action.yml"
fi

if [ ! -f "$ACTION_YML" ]; then
  echo "GitHub Action YAML file not found at $ACTION_YML"
  echo "Usage: $0 [action-yml-file]"
  exit 1
fi

echo "Fixing name tags in GitHub Action: $ACTION_YML"
cp "$ACTION_YML" "$ACTION_YML.orig"

# Fix tags with properly escaped sed expressions
cat "$ACTION_YML" | sed "s/\<n\>/\<name\>/g" | sed "s/\<\/n\>/\<\/name\>/g" > "$ACTION_YML.fixed"

# Check if the file was modified
if diff -q "$ACTION_YML" "$ACTION_YML.fixed" >/dev/null; then
  echo "No changes needed to $ACTION_YML"
  rm "$ACTION_YML.fixed"
else
  echo "Fixed name tags in $ACTION_YML"
  mv "$ACTION_YML.fixed" "$ACTION_YML"
fi

# Verify fix worked
if grep -q "<n>" "$ACTION_YML" || grep -q "</n>" "$ACTION_YML"; then
  echo "WARNING: Still found name tag issues!"
  grep -n "<n>" "$ACTION_YML" || true
  grep -n "</n>" "$ACTION_YML" || true
else
  echo "✓ Successfully fixed all name tags in GitHub Action file!"
fi
