# AndroidX Compatibility Guide

This document outlines the minimum supported AndroidX versions for the PayPal Messages Android SDK and provides guidance for consumers who may be using older AndroidX versions.

## Overview

The SDK is tested and validated against specific AndroidX versions. To ensure compatibility and avoid dependency conflicts, consumers should ensure their AndroidX dependencies meet or exceed the minimum versions documented below.

## Minimum Supported Versions

| Artifact | Baseline Version | Minimum Safe Version | Notes | Test Date |
|----------|-----------------|---------------------|-------|-----------|
| `androidx.core:core-ktx` | 1.10.1 | 1.8.0 | Core AndroidX extensions | Dec 2024 |
| `androidx.appcompat:appcompat` | 1.6.1 | 1.4.2 | AppCompat library | Dec 2024 |
| `androidx.activity:activity-compose` | 1.7.2 | 1.6.1 | Activity Compose integration | Dec 2024 |
| `androidx.compose:compose-bom` | 2023.05.01 | 2023.01.00 | Compose BOM (Bill of Materials) | Dec 2024 |
| `com.google.android.material:material` | 1.9.0 | 1.8.0 | Material Design Components | Dec 2024 |

**Note:** Minimum safe versions have been validated through CI matrix testing. All listed versions passed build and unit test validation.

## Version Override Testing

The SDK supports version override testing via Gradle properties. This allows validation of compatibility with different AndroidX versions:

```bash
./gradlew :library:assembleRelease \
  -PandroidxCoreVersion=1.9.0 \
  -PappcompatVersion=1.5.1 \
  -PactivityComposeVersion=1.6.1 \
  -PcomposeBomVersion=2023.03.00 \
  -PmaterialVersion=1.8.0
```

## Dependency Resolution

### Constraints

The SDK uses Gradle dependency constraints to enforce minimum versions. These constraints:
- Prevent consumers from using versions that are too old
- Ensure consistent dependency resolution across the dependency graph
- Can be overridden for testing purposes (using `prefer()` instead of `strictly()`)

### Dependency Locking

The SDK uses Gradle dependency locking to ensure reproducible builds. The lockfile (`library/gradle.lockfile`) is committed to version control and should be updated during releases.

## Consumer Guidance

### If Using Older AndroidX Versions

1. **Check Compatibility**: Ensure your AndroidX versions meet or exceed the minimum safe versions documented above.

2. **Handle Conflicts**: If you encounter version conflicts:
   - Update your AndroidX dependencies to meet minimum requirements
   - Use Gradle's dependency resolution strategies to force specific versions
   - Consider excluding transitive dependencies if necessary (though this is not recommended)

3. **Material Components**: The SDK exposes Material Components as `api` dependencies to ensure resources (like `bottomSheetStyle`) are available to consumers. If you need to use a different Material version:
   - Ensure it's compatible with the SDK's minimum version
   - Test thoroughly to ensure resources resolve correctly

4. **Compose BOM**: When using Compose BOM:
   - The SDK uses Compose BOM `2023.05.01` as baseline
   - Ensure your Compose BOM version is compatible with Kotlin 1.8.22 and Compose compiler extension 1.4.8
   - You can override the BOM version, but ensure all Compose artifacts are compatible

### OkHttp Compatibility

The SDK bundles OkHttp 4.8.0 as an `implementation` dependency (not exposed in public API). This means:
- Consumers can use any OkHttp version (3.x or 4.x) without conflicts
- The SDK's internal OkHttp usage won't force a specific version on consumers
- If you need to use OkHttp APIs directly, ensure compatibility with the SDK's internal usage

## Testing Matrix

The following version combinations have been validated through CI matrix testing:

### Core KTX
- Baseline: 1.10.1 ✅
- Tested: 1.9.0 ✅, 1.8.0 ✅
- **Minimum Safe: 1.8.0**

### AppCompat
- Baseline: 1.6.1 ✅
- Tested: 1.5.1 ✅, 1.4.2 ✅
- **Minimum Safe: 1.4.2**

### Activity Compose
- Baseline: 1.7.2 ✅
- Tested: 1.6.1 ✅
- **Minimum Safe: 1.6.1**

### Compose BOM
- Baseline: 2023.05.01 ✅
- Tested: 2023.03.00 ✅, 2023.01.00 ✅
- **Minimum Safe: 2023.01.00**

### Material
- Baseline: 1.9.0 ✅
- Tested: 1.8.0 ✅
- **Minimum Safe: 1.8.0**

## CI Matrix Testing

The SDK includes CI jobs that test various AndroidX version combinations to validate compatibility. Results are documented in this file as they become available.

## Updates

This document will be updated as:
- Matrix testing completes and minimum safe versions are determined
- New AndroidX versions are validated
- Breaking changes require version updates

For the latest compatibility information, refer to the SDK's release notes and this document.

