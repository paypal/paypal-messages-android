#!/bin/bash
# Fix name tags in GitHub Action files
set -e

ACTION_FILE="/Users/grablack/Code/paypal-messages-android/.github/actions/publish_maven_central/action.yml"

if [ ! -f "$ACTION_FILE" ]; then
  echo "Error: Action file not found: $ACTION_FILE"
  exit 1
fi

echo "Fixing name tags in GitHub Action file: $ACTION_FILE"
perl -i -pe 's/<n>([^<]+)<\/n>/<name>$1<\/name>/g' "$ACTION_FILE"

echo "Action file fixed"
grep -n "<name>" "$ACTION_FILE" || echo "No name tags found"
