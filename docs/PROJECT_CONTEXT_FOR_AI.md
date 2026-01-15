# Android CI/CD Shared Library - Complete Project Context

## Project Overview

This is a **Jenkins Shared Library** for automating Android React Native CI/CD with:
- Docker-based build agents (from Nexus registry: 43.88.89.25:5000)
- Google Play Store deployment via Fastlane
- Jest unit testing + Appium E2E testing
- Slack notifications
- Support for `dev` and `prod` environments
- Support for `debug` and `release` builds

---

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Groovy | 3.x | Shared library code |
| Java | 17 | Android builds |
| Node.js | 20 | React Native |
| Android SDK | 34 | Android platform |
| Gradle | 8.x | Build tool |
| Fastlane | 2.225 | Play Store deployment |
| Docker | - | Build agents |
| Jest | - | Unit testing |
| Appium/WebDriverIO | - | E2E testing |

---

## Project Structure

```
mobile cicd/
├── Dockerfile                    # Build agent Docker image
├── README.md                     # Quick start guide
│
├── docs/                         # Documentation
│   ├── ARCHITECTURE.md           # System architecture
│   ├── API_REFERENCE.md          # All APIs
│   ├── FILE_EXPLANATIONS.md      # File-by-file explanation
│   ├── FOLDER_STRUCTURE.md       # Directory structure
│   ├── IMPLEMENTATION_PLAN.md    # How it was built
│   ├── PIPELINE_FLOW.md          # Pipeline stages
│   ├── PROJECT_CONTEXT_FOR_AI.md # This file (for ChatGPT)
│   └── SETUP_GUIDE.md            # Setup instructions
│
├── vars/                         # Global Pipeline Functions
│   ├── androidPipeline.groovy    # Main pipeline
│   ├── androidBuild.groovy       # Build APK/AAB
│   ├── androidTest.groovy        # Jest/Appium tests
│   ├── androidDeploy.groovy      # Play Store deploy
│   ├── notifyBuild.groovy        # Slack notifications
│   ├── withAndroidSigning.groovy # Signing credentials
│   ├── gitCheckout.groovy        # Git operations
│   └── setupEnv.groovy           # Environment setup
│
├── src/jenkins/pipeline/         # Core Classes
│   ├── mobile/                   # AndroidBuilder
│   ├── testing/                  # JestRunner, AppiumRunner
│   ├── ci/                       # FastlaneExecutor
│   ├── notification/             # SlackNotifier
│   ├── distribution/             # PlayStoreDistributor
│   ├── models/                   # BuildConfig
│   ├── config/                   # FastlaneConfig
│   ├── interfaces/               # IBuilder, ITestRunner
│   ├── enums/                    # BuildType, Environment
│   ├── exceptions/               # Custom exceptions
│   ├── utils/                    # Utilities
│   ├── logging/                  # PipelineLogger
│   ├── secrets/                  # SecretsManager
│   └── validators/               # ConfigValidator
│
└── resources/templates/android/  # Fastlane templates
    ├── Fastfile.template
    ├── Appfile.template
    └── Gemfile.template
```

---

## Pipeline Stages

```
1. Checkout       → Git checkout
2. Setup          → Make gradlew executable
3. Dependencies   → yarn install / npm ci
4. Lint           → npm run lint
5. Build          → ./gradlew assemble/bundle
6. Unit Tests     → Jest (npm test)
7. E2E Tests      → Appium (optional)
8. Deploy         → Fastlane → Play Store (if DEPLOY=true)
9. Notify         → Slack notification
```

---

## Key APIs

### androidPipeline()
```groovy
androidPipeline(
    appName: 'MyApp',
    dockerRegistry: '43.88.89.25:5000',
    dockerImage: 'react-native-android',
    keystoreCredentialsId: 'android-keystore',
    keystorePasswordCredentialsId: 'keystore-password',
    playStoreCredentialsId: 'google-play-json-key',
    playStoreTrack: 'internal',
    slackChannel: '#builds',
    runUnitTests: true,
    runE2ETests: false
)
```

### androidBuild()
```groovy
def result = androidBuild(
    appName: 'MyApp',
    buildType: 'release',    // debug / release
    environment: 'prod',     // dev / prod
    outputType: 'aab'        // apk / aab
)
// Returns: {success, artifactPath, version, error}
```

### androidTest()
```groovy
// Unit tests
def result = androidTest(framework: 'jest')

// E2E tests
def result = androidTest(
    framework: 'appium',
    emulatorUrl: 'http://emulator:5555'
)
```

### androidDeploy()
```groovy
androidDeploy(
    artifactPath: 'app.aab',
    track: 'internal',        // internal/alpha/beta/production
    rolloutPercentage: 100
)
```

---

## Jenkins Credentials Required

| ID | Type | Description |
|----|------|-------------|
| `android-keystore` | Secret file | Signing keystore (.jks) |
| `android-keystore-password` | Secret text | Keystore password |
| `google-play-json-key` | Secret file | Play Store service account JSON |
| `slack-webhook` | Secret text | Slack webhook URL |

---

## Docker Image Contents

- **Base:** openjdk:17-jdk-slim from Nexus
- **Node.js 20** with yarn
- **Android SDK 34** with build-tools
- **Fastlane 2.225** for deployment
- **BundleTool** for AAB processing

---

## Build Variants

| Environment | Build Type | Variant Name | Output |
|-------------|------------|--------------|--------|
| dev | debug | devDebug | APK |
| dev | release | devRelease | AAB |
| prod | debug | prodDebug | APK |
| prod | release | prodRelease | AAB → Play Store |

---

## Key Design Decisions

1. **Simple classes over patterns** - Map constructors instead of Builder pattern
2. **Fastlane for deployment only** - Lint/tests run via npm/Gradle directly
3. **Single Docker image** - Works for all RN versions (RN version in package.json)
4. **Separate test stages** - Unit Tests (Jest) and E2E Tests (Appium) are separate

---

## Easy Configuration Points

### Change Default Values
Edit `vars/androidPipeline.groovy` lines 14-19:
```groovy
String dockerRegistry = config.dockerRegistry ?: 'your-registry.com:5000'
```

### Add New Pipeline Parameters
Edit `vars/androidPipeline.groovy` in parameters block:
```groovy
booleanParam(name: 'YOUR_PARAM', defaultValue: false, description: 'Description')
```

### Add New Pipeline Stage
```groovy
stage('Your Stage') {
    steps {
        sh 'your-command'
    }
}
```

---

## Usage Example (Jenkinsfile)

```groovy
@Library('mobile-ci-cd') _

androidPipeline(
    appName: 'MyApp',
    keystoreCredentialsId: 'android-keystore',
    keystorePasswordCredentialsId: 'keystore-password',
    playStoreCredentialsId: 'google-play-json-key',
    slackChannel: '#mobile-builds'
)
```

---

## For ChatGPT Usage

Copy this entire document and paste into ChatGPT. Then ask:
- "Help me configure Jenkins with this shared library"
- "Explain how the androidPipeline function works"
- "How do I add a new stage to the pipeline?"
- "Help me troubleshoot build failures"
