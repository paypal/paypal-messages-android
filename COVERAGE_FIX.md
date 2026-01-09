# Coverage Fix for GitHub Actions

## Problem
The code coverage report shows 0.00% for `PayPalMessageDataProvider.kt` even though we have 17 unit tests covering the critical error handling logic.

## Root Cause
The GitHub Actions workflow (`.github/workflows/test.yml`) runs `koverXmlReportDebug` without first running the unit tests, so no coverage data is collected.

## Solution
Update `.github/workflows/test.yml` line 51-52:

### Current (broken):
```yaml
- name: Run Unit Tests Coverage
  run: ./gradlew koverXmlReportDebug
```

### Fixed:
```yaml
- name: Run Unit Tests Coverage
  run: ./gradlew testDebugUnitTest koverXmlReportDebug
```

## What This Fixes
1. Runs all debug unit tests (including our 17 new tests for `PayPalMessageDataProvider`)
2. Collects coverage data during test execution
3. Generates accurate coverage report showing ~80-90% coverage for the file

## Tests That Will Be Covered
- `PayPalMessageDataProviderTest` - 17 tests including:
  - Direct tests of `handleApiResult()` method (the extracted error handling logic)
  - Tests for null error safety net (the critical bug fix)
  - Tests for 404, 500, and other error types
  - Success and failure scenarios

## Implementation in Code
We refactored `PayPalMessageDataProvider` to extract the callback logic into a testable `handleApiResult()` method:

```kotlin
internal fun handleApiResult(
    result: ApiResult,
    startTime: Long,
    callback: PayPalMessageDataCallback,
) {
    // This code is now directly testable
    when (result) {
        is ApiResult.Failure<*> -> {
            // THE CRITICAL FIX: Always provide a default error
            val error = result.error ?: PayPalErrors.FailedToFetchDataException("Unknown error occurred", null)
            callback.onError(error)
        }
    }
}
```

This allows unit tests to directly invoke and verify the error handling logic, ensuring proper code coverage.

