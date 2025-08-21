# CLAUDE.md - PayPal Messages Android SDK

This guide helps Claude instances understand and work effectively with the PayPal Messages Android SDK codebase.

## Project Overview

**Purpose**: An Android SDK library that allows easy integration of PayPal Credit Messages into Android applications. The library displays messages about PayPal's pay-later products and offers, with customizable styling and modal interactions.

**Current Status**: Version 1.1.7, available on Maven Central. The library is production-ready with active development.

**Key Features**:
- PayPal Credit Message component with customizable styling 
- Modal dialogs for offer details
- Jetpack Compose and XML View support
- Analytics integration
- Multiple PayPal environments (sandbox, staging, production)

## Project Structure

```
paypal-messages-android/
├── library/                    # Main SDK library module
├── demo/                      # Demo app showcasing SDK usage
├── .github/                   # CI/CD workflows and actions
├── scripts/                   # Utility scripts
├── docs/                      # Documentation
├── gradle/                    # Gradle configuration
└── readme-images/             # Images for documentation
```

### Key Modules

1. **`:library`** - The main SDK library
   - Package: `com.paypal.messages`
   - Artifact ID: `paypal-messages` 
   - Main components: `PayPalMessageView`, `PayPalComposableMessage`, Modal system

2. **`:demo`** - Demo application 
   - Package: `com.paypal.messagesdemo`
   - Contains XML and Jetpack Compose examples
   - Useful for testing SDK integration

## Build System

**Build Tool**: Gradle with Android Gradle Plugin 8.0.2
**Java Version**: JDK 17 
**Kotlin Version**: 1.8.22
**Target SDK**: Android 34 (compile), minimum SDK 23

### Key Build Files
- `/build.gradle` - Root project configuration with publishing setup
- `/library/build.gradle` - Library module configuration  
- `/demo/build.gradle` - Demo app configuration
- `/settings.gradle` - Multi-module project settings
- `/gradle.properties` - Project-wide properties

## Common Commands

### Building
```bash
# Build library AAR
./gradlew :library:assemble

# Build demo app
./gradlew :demo:assemble

# Build everything
./gradlew assemble

# Clean build
./gradlew clean
```

### Testing
```bash
# Run all tests (optimized)
./run-all-tests.sh

# Unit tests only
./gradlew :library:testDebugUnitTest

# Memory-intensive tests (run separately)
./gradlew :library:testMemoryIntensiveClasses

# Instrumentation tests
./gradlew connectedCheck

# Test coverage report
./gradlew koverXmlReportDebug
```

### Code Quality
```bash
# Kotlin linting
./gradlew ktLint

# Kotlin formatting
./gradlew ktFormat
```

### Publishing
```bash
# Prepare artifacts for Maven Central
./prepare-nmcp-bundle.sh

# Publish to Maven Central (requires credentials)
./gradlew publishToCentralPortal

# Check deployment status
./gradlew checkCentralPortalDeployment

# Change version
./gradlew -PversionParam=1.2.3 changeReleaseVersion
```

## Key Components & Architecture

### Main SDK Components

1. **PayPalMessageView** (`/library/src/main/java/com/paypal/messages/PayPalMessageView.kt`)
   - Primary XML-based message component
   - Handles styling, content fetching, and user interactions
   - Configurable via XML attributes or programmatically

2. **PayPalComposableMessage** (`/library/src/main/java/com/paypal/messages/PayPalComposableMessage.kt`)
   - Jetpack Compose version of the message component
   - Modern declarative UI approach

3. **Modal System**
   - `PayPalModalActivity` - Full-screen modal activity
   - `ModalFragment` - Bottom sheet modal
   - `RoundedWebView` - Custom WebView for modal content

4. **Data Provider** (`/library/src/main/java/com/paypal/messages/data/PayPalMessageDataProvider.kt`)
   - Handles API communication
   - Manages message content fetching
   - Creates click handlers for modal interactions

### Configuration Classes
- `PayPalMessageConfig` - Main configuration container
- `PayPalMessageData` - Message data (amount, client ID, etc.)
- `PayPalMessageStyle` - Styling options (color, logo, alignment)
- `PayPalEnvironment` - Environment settings (sandbox, staging, production)

### Analytics System
- `AnalyticsLogger` - Event logging
- `AnalyticsComponent` - Component tracking
- `AnalyticsEvent` - Event definitions

## Development Setup

### Prerequisites
- Android Studio with JDK 17
- Android SDK with API level 23-34
- Git

### Initial Setup
1. Clone the repository
2. Set demo client ID in `/demo/src/main/res/values/locals.xml`
3. Run ignore files script: `./scripts/ignore-files.sh -y`
4. Set environment variables (optional):
   ```bash
   export UPSTREAM_ANDROID_STAGE_URL=""
   export UPSTREAM_ANDROID_STAGE_VPN_URL=""
   export UPSTREAM_ANDROID_LOCAL_URL=""
   ```

### Running the Demo
- Main activity: `XmlActivity` (traditional XML layouts)
- Configure run configuration to launch specific activity
- Use debug build type for development

## Testing Strategy

### Test Types
1. **Unit Tests** - Fast, isolated tests
2. **Instrumentation Tests** - Android-specific tests requiring emulator
3. **Memory-Intensive Tests** - Run separately to avoid memory issues

### Test Organization
- Unit tests: `/library/src/test/`
- Instrumentation tests: `/library/src/androidTest/`
- Coverage target: 85% minimum

### Known Issues
- Some tests require isolation due to memory usage
- Instrumentation tests run on API level 23 emulator
- ProGuard/R8 compatibility requires specific consumer rules

## CI/CD Pipeline

### GitHub Actions Workflows
- **Build** (`build.yml`) - Library assembly and verification
- **Test** (`test.yml`) - Unit, instrumentation, and coverage tests
- **Lint** (`lint.yml`) - Code quality checks
- **Release** (`release.yml`) - Automated releases to Maven Central

### Key CI Jobs
- Build verification with ProGuard rule testing
- Reflection crash fix verification
- Publishing task verification
- Coverage reporting (85% minimum)

## Publishing & Release

### Maven Central Publishing
- **Artifact**: `com.paypal.messages:paypal-messages`
- **Repository**: Maven Central via Sonatype Central Portal
- **Signing**: GPG signatures required
- **Automation**: GitHub Actions handles releases

### Required Secrets for Publishing
- `SONATYPE_NEXUS_USERNAME`
- `SONATYPE_NEXUS_PASSWORD` 
- `SIGNING_KEY_ID`
- `SIGNING_KEY_PASSWORD`
- `SIGNING_KEY_FILE`

### Release Process
1. Update version in code
2. Create release branch
3. CI builds and tests
4. Manual approval for release
5. Automated publishing to Maven Central

## Important Conventions

### Code Style
- Kotlin coding standards enforced by kotlinter
- 4-space indentation
- Meaningful variable and function names

### Package Structure
```
com.paypal.messages/
├── analytics/          # Analytics and tracking
├── config/            # Configuration classes
├── data/              # Data providers and handlers
├── extensions/        # Kotlin extensions
├── io/                # Network and API classes
├── utils/             # Utility classes
└── *.kt               # Main UI components
```

### Testing Patterns
- Use JUnit 5 for unit tests
- MockK for mocking in Kotlin
- Separate memory-intensive tests
- Test both XML and Compose components

## Common Issues & Solutions

### ProGuard/R8 Issues
- Consumer rules in `/library/consumer-rules.pro`
- Protects reflection-based code
- Test minification with demo app

### Memory Issues in Tests
- Use `testMemoryIntensiveClasses` task
- Run problematic tests in isolation
- Increase heap size if needed

### Environment Variables
- Required for demo app functionality
- Set in shell config for development
- Used in build configs for URL endpoints

## Dependencies

### Key Dependencies
- **OkHttp 4.8.0** - HTTP client (bundled in AAR)
- **Gson 2.9.1** - JSON parsing
- **AndroidX** - Android support libraries
- **Jetpack Compose** - Modern UI toolkit

### Dependency Management
- Versions managed in root `build.gradle`
- BOM (Bill of Materials) for Compose
- Constraints for version alignment

## File Locations Reference

### Configuration Files
- `/gradle.properties` - Project properties
- `/library/pom.xml` - Maven publishing configuration
- `/library/consumer-rules.pro` - ProGuard consumer rules

### Key Source Files
- `/library/src/main/java/com/paypal/messages/PayPalMessageView.kt` - Main XML component
- `/library/src/main/java/com/paypal/messages/PayPalComposableMessage.kt` - Compose component
- `/library/src/main/java/com/paypal/messages/data/PayPalMessageDataProvider.kt` - Data layer

### Development Files
- `/DEVELOPMENT.md` - Detailed development instructions
- `/demo/src/main/res/values/locals.xml` - Demo configuration
- `/.github/workflows/` - CI/CD configurations

This guide should provide sufficient context for Claude to understand and work effectively with the PayPal Messages Android SDK codebase.

## Release Rules

- Bundle okhttp into aar
- AAR should be primary artifact
- Deploy only the library module
- Sign the POM