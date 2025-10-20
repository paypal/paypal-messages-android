# Manifest Merger Test Module

## Purpose

This test module verifies that the PayPal Messages library does not cause Android Manifest merger conflicts when integrated into apps.

## Problem Being Tested

The library's `AndroidManifest.xml` declares:
```xml
<application android:usesCleartextTraffic="@string/clear_text_config">
```

When an app integrates the library and also declares its own `android:usesCleartextTraffic` attribute, the Android manifest merger needs to resolve the conflict. This test ensures that:

1. The manifest merger succeeds without errors
2. The app's declared value takes precedence (or merges correctly)
3. No build-time conflicts occur

## Test Approach

This module simulates a typical Android app that:
- Declares its own `android:usesCleartextTraffic="false"` in its manifest
- Integrates the PayPal Messages library as a dependency

The CI/CD pipeline builds this module to verify no manifest merger conflicts occur.

## Running Locally

```bash
# Build the test module
./gradlew :manifest-merger-test:assembleDebug

# View the merged manifest
cat manifest-merger-test/build/intermediates/merged_manifests/debug/AndroidManifest.xml
```

## CI/CD Integration

This test runs automatically in GitHub Actions as part of the `test.yml` workflow.

Job: `test_manifest_merger`

## Expected Behavior

**Current Status:** This test is expected to **FAIL** on the current branch.

There is a fix in progress on another branch that resolves the `usesCleartextTraffic` manifest merger conflict. Once that fix is merged, this test should pass.

**When the fix is applied:**
- Build should succeed without manifest merger errors
- Merged manifest should contain a valid `usesCleartextTraffic` attribute
- No warnings about attribute conflicts should appear

## Purpose

This test validates that the manifest merger conflict has been properly resolved. It serves as:
- A regression test to prevent the issue from being reintroduced
- Validation that the fix branch correctly resolves the conflict
- Continuous verification in CI/CD
