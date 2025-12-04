# AndroidX Compatibility Guide

This document outlines the minimum supported AndroidX versions for the PayPal Messages Android SDK and provides guidance for consumers who may be using older AndroidX versions.

## Overview

The SDK is tested and validated against specific AndroidX versions. To ensure compatibility and avoid dependency conflicts, consumers should ensure their AndroidX dependencies meet or exceed the minimum versions documented below.

## Minimum Supported Versions

| Artifact | SDK Version | Minimum Required | Notes | Test Date |
|----------|-------------|------------------|-------|-----------|
| `androidx.core:core-ktx` | 1.8.0 | 1.8.0 | Core AndroidX extensions | Dec 2024 |
| `androidx.appcompat:appcompat` | 1.4.2 | 1.4.2 | AppCompat library | Dec 2024 |
| `androidx.activity:activity-compose` | 1.7.2 | 1.7.2 | Activity Compose integration | Dec 2024 |
| `androidx.compose:compose-bom` | 2023.05.01 | 2023.05.01 | Compose BOM (Bill of Materials) | Dec 2024 |
| `com.google.android.material:material` | 1.8.0 | 1.8.0 | Material Design Components (provided by SDK) | Dec 2024 |

**Important Notes:**
- The SDK declares **minimum safe versions** to maximize compatibility with merchant apps.
- Gradle will resolve to the higher version if your app uses a newer version.
- **Material Components**: The SDK provides Material as an `api` dependency. If your app uses a higher version, Gradle will resolve to your version automatically.

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
   - **Recommended**: Update your AndroidX dependencies to meet minimum requirements
   - **Alternative**: Use Gradle's dependency resolution strategies to force compatible versions
   - **Last Resort**: Consider excluding transitive dependencies (though this is not recommended and may cause runtime issues)

### Resolving Version Conflicts

If you encounter dependency conflicts like:
```
Conflicts found for the following modules:
  - com.google.android.material:material between versions 1.11.0 and 1.4.0-beta01
  - androidx.core:core between versions 1.9.0, 1.8.0, 1.6.0...
```

**Solution 1: Update Dependencies (Recommended)**
Update your `build.gradle` to use compatible versions:
```gradle
dependencies {
    // Ensure minimum versions
    implementation 'com.google.android.material:material:1.8.0' // or higher
    implementation 'androidx.core:core-ktx:1.8.0' // or higher
    implementation 'androidx.appcompat:appcompat:1.4.2' // or higher
    // ... other dependencies
}
```

**Solution 2: Force Resolution (If you can't update immediately)**
Add to your app's `build.gradle`:
```gradle
configurations.all {
    resolutionStrategy {
        // Force compatible versions
        force 'com.google.android.material:material:1.8.0'
        force 'androidx.core:core-ktx:1.8.0'
        force 'androidx.appcompat:appcompat:1.4.2'
        // Prefer newer versions for other conflicts
        preferProjectModules()
    }
}
```

**Solution 3: Exclude Conflicting Dependencies (Not Recommended)**
Only if absolutely necessary:
```gradle
dependencies {
    implementation('com.paypal.messages:library:1.1.14') {
        // Exclude only if you're providing your own compatible version
        exclude group: 'com.google.android.material', module: 'material'
    }
    // Then provide your own version
    implementation 'com.google.android.material:material:1.8.0'
}
```

3. **Material Components**: The SDK provides Material 1.8.0 as an `api` dependency. If your app uses a higher version (e.g., 1.11.0), Gradle will automatically resolve to your version. No action needed.

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
- SDK Version: 1.8.0 ✅
- Tested: 1.10.1 ✅, 1.9.0 ✅, 1.8.0 ✅
- **Minimum Safe: 1.8.0**

### AppCompat
- SDK Version: 1.4.2 ✅
- Tested: 1.6.1 ✅, 1.5.1 ✅, 1.4.2 ✅
- **Minimum Safe: 1.4.2**

### Activity Compose
- SDK Version: 1.7.2 ✅
- Tested: 1.7.2 ✅
- **Minimum Safe: 1.7.2** (required for stable Compose APIs)

### Compose BOM
- SDK Version: 2023.05.01 ✅
- Tested: 2023.05.01 ✅
- **Minimum Safe: 2023.05.01** (required for stable Compose APIs)

### Material
- SDK Version: 1.8.0 ✅
- Tested: 1.9.0 ✅, 1.8.0 ✅
- **Minimum Safe: 1.8.0** (provided by SDK)

## CI Matrix Testing

The SDK includes CI jobs that test various AndroidX version combinations to validate compatibility. Results are documented in this file as they become available.

## Updates

This document will be updated as:
- Matrix testing completes and minimum safe versions are determined
- New AndroidX versions are validated
- Breaking changes require version updates

For the latest compatibility information, refer to the SDK's release notes and this document.

