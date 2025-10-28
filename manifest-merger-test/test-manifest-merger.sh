#!/bin/bash

# Test script to verify manifest merger doesn't cause conflicts
# This script builds the test module and verifies the merged manifest

set -e

echo "🔍 Testing manifest merger with usesCleartextTraffic conflict..."
echo ""

# Build the test module
echo "Building manifest-merger-test module..."
./gradlew :manifest-merger-test:assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Manifest merger succeeded - no conflict detected!"
else
    echo "❌ ERROR: Manifest merger failed!"
    exit 1
fi

echo ""
echo "🔍 Verifying merged manifest content..."

# Path to merged manifest
MERGED_MANIFEST="manifest-merger-test/build/intermediates/merged_manifests/debug/AndroidManifest.xml"

if [ ! -f "$MERGED_MANIFEST" ]; then
    echo "❌ ERROR: Merged manifest not found at $MERGED_MANIFEST"
    exit 1
fi

echo ""
echo "📄 Merged manifest content:"
echo "---"
cat "$MERGED_MANIFEST"
echo "---"
echo ""

# Verify usesCleartextTraffic is present
if grep -q "usesCleartextTraffic" "$MERGED_MANIFEST"; then
    echo "✅ usesCleartextTraffic attribute found in merged manifest"

    # Show the exact line
    echo ""
    echo "📋 usesCleartextTraffic declaration:"
    grep "usesCleartextTraffic" "$MERGED_MANIFEST"
else
    echo "❌ WARNING: usesCleartextTraffic attribute not found in merged manifest"
fi

echo ""
echo "🎉 Manifest merger test completed successfully!"
