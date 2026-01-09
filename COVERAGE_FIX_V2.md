# Code Coverage Fix - Root Cause and Solution

## Problem

The code coverage report in GitHub Actions was showing 0% despite:
1. Tests being added
2. The workflow being updated to run `testDebugUnitTest` before `koverXmlReportDebug`

## Root Causes

There were **two separate issues**:

### Issue 1: JUnit 4 Tests Not Running
The project uses **JUnit 5** (`useJUnitPlatform()` in `build.gradle`), but the new tests for error handling use **Robolectric**, which requires **JUnit 4** (`@RunWith(RobolectricTestRunner::class)`).

When Gradle's `useJUnitPlatform()` is configured without the JUnit Vintage engine, **JUnit 4 tests are silently skipped**. This means:
- The Robolectric tests never ran
- No coverage data was generated for the new code paths
- The coverage report remained at 0%

### Issue 2: Resources Not Found
After fixing Issue 1, tests started running but failed with `Resources$NotFoundException`. This is because:
- `PayPalMessageView` inflates layouts in its constructor (`R.layout.paypal_message_view`)
- Robolectric needs `includeAndroidResources = true` to access library resources
- Without this setting, Robolectric can't find the layouts and throws exceptions

## Solutions

### Solution 1: Support Both JUnit 4 and JUnit 5

Configure Gradle to support **both JUnit 4 and JUnit 5** tests by:

1. **Adding JUnit Vintage Engine** (runs JUnit 4 tests on JUnit 5 platform):
```gradle
testRuntimeOnly 'org.junit.vintage:junit-vintage-engine:5.10.0'
```

2. **Configuring JUnit Platform** to include both engines AND enable Android resources:
```gradle
testOptions {
    execution 'ANDROIDX_TEST_ORCHESTRATOR'
    unitTests {
        // Enable Robolectric to access Android resources in unit tests
        includeAndroidResources = true
        all {
            // Support both JUnit 4 and JUnit 5
            useJUnitPlatform {
                includeEngines 'junit-jupiter', 'junit-vintage'
            }
            testLogging {
                events 'passed', 'skipped', 'failed'
            }
        }
    }
}
```

## Files Changed

### 1. `library/build.gradle`
- Added `junit-vintage-engine` dependency (enables JUnit 4 tests)
- Updated `useJUnitPlatform()` to explicitly include both engines
- Added `includeAndroidResources = true` (enables Robolectric resource access)

### 2. Test Files (kept as JUnit 4 with Robolectric)
- `library/src/test/java/com/paypal/messages/data/PayPalMessageDataProviderTest.kt`
- `library/src/test/java/com/paypal/messages/PayPalMessageViewErrorTest.kt`
- `library/src/test/java/com/paypal/messages/ui/BottomSheetStyleInflationTest.kt`

These files use:
- `@RunWith(RobolectricTestRunner::class)` (JUnit 4)
- `@Config(sdk = [28])` (Robolectric)
- JUnit 4 assertions (`org.junit.Assert.*`)
- JUnit 4 lifecycle (`@Before`, `@After`, `@Test`)

## Why This Works

1. **JUnit Vintage Engine** acts as a bridge, allowing JUnit 4 tests to run on the JUnit 5 platform
2. **includeAndroidResources = true** tells Gradle to make Android layouts, themes, and resources available to Robolectric
3. **Robolectric tests** (JUnit 4) can now:
   - Execute successfully (not skipped)
   - Inflate layouts like `R.layout.paypal_message_view`
   - Access themes like `R.style.Theme_PayPalModal`
   - Generate coverage data
4. **Existing JUnit 5 tests** continue to work via the Jupiter engine
5. **Kover** can now collect coverage from both test types

## Additional Fix: Callback Invocation Consistency

During testing, discovered that `onLoading()` method had an ambiguous reference. Fixed by ensuring all three callback methods explicitly use `this.` to reference the property:

```kotlin
override fun onLoading() {
    this.onLoading.invoke()  // Explicit property reference
}

override fun onSuccess(response: ApiMessageData.Response, duration: Int) {
    this.onSuccess.invoke()  // Already explicit
}

override fun onError(error: PayPalErrors.Base) {
    this.onError.invoke(error)  // Now explicit
}
```

This prevents Kotlin from potentially confusing the method name with the property name.

## Expected Outcome

After these changes:
- All tests (JUnit 4 + JUnit 5) will run in CI
- Tests will pass without `Resources$NotFoundException`
- Coverage data will be generated for the new error handling code
- The coverage report should show > 0% coverage
- Specifically, `PayPalMessageDataProvider.handleApiResult()` should show coverage

## Verification

To verify locally (if SSL issues are resolved):
```bash
./gradlew testDebugUnitTest koverXmlReportDebug
cat library/build/reports/kover/reportDebug.xml
```

In CI, the GitHub Actions workflow will:
1. Run all unit tests (JUnit 4 + JUnit 5)
2. Generate coverage report
3. Display coverage in PR comments

