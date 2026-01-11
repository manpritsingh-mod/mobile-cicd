# File-by-File Explanation

## Root Files

### Dockerfile
**Purpose:** Defines the Docker build environment for Android React Native apps.

**Contains:**
- OpenJDK 17 (for Gradle)
- Node.js 20 (for React Native)
- Android SDK 34 with build-tools
- Fastlane 2.225 (for Play Store deployment)
- Yarn package manager

**Used by:** Jenkins pulls this image to run pipeline stages inside a container.

---

### README.md
**Purpose:** Quick start guide for new users.

---

## vars/ Folder (8 files)

These are **global pipeline functions** that can be called directly from Jenkinsfile.

| File | Purpose | When Called |
|------|---------|-------------|
| `androidPipeline.groovy` | Main pipeline orchestrator with all stages | Entry point from Jenkinsfile |
| `androidBuild.groovy` | Executes Gradle build (APK/AAB) | Build stage |
| `androidTest.groovy` | Runs Jest or Appium tests | Test stages |
| `androidDeploy.groovy` | Deploys to Google Play Store | Deploy stage |
| `notifyBuild.groovy` | Sends Slack notifications | Success/Failure |
| `withAndroidSigning.groovy` | Wraps code with signing credentials | Release builds |
| `gitCheckout.groovy` | Git checkout with options | Checkout stage |
| `setupEnv.groovy` | Sets up build environment | Setup stage |

---

## src/jenkins/pipeline/mobile/ (2 files)

### AndroidBuilder.groovy
**Purpose:** Core class that handles Android app building.

**Methods:**
- `build(config)` - Runs Gradle build
- `clean()` - Cleans build directory
- `installDependencies()` - Runs npm/yarn install
- `runLint()` - Runs ESLint
- `validateEnvironment()` - Checks ANDROID_HOME

### BuilderFactory.groovy
**Purpose:** Factory pattern to create platform-specific builders.

---

## src/jenkins/pipeline/testing/ (3 files)

### JestRunner.groovy
**Purpose:** Runs Jest unit tests for React Native JavaScript code.

**Methods:**
- `runTests()` - Executes `npm test`
- `isAvailable()` - Checks if package.json exists

### AppiumRunner.groovy
**Purpose:** Runs Appium E2E tests using Docker emulator.

**Methods:**
- `runTests()` - Executes WebDriverIO tests
- `withApp(path)` - Sets APK path for testing
- `isAvailable()` - Checks if wdio.conf.js exists

### TestOrchestrator.groovy
**Purpose:** Coordinates multiple test runners.

---

## src/jenkins/pipeline/ci/ (2 files)

### FastlaneExecutor.groovy
**Purpose:** Executes Fastlane commands for build and deployment.

**Methods:**
- `runLane(lane, options)` - Runs any Fastlane lane
- `deployToPlayStore(aabPath, track, rollout)` - Deploys to Play Store

### DockerImageManager.groovy
**Purpose:** Manages Docker image operations (pull, build, push).

---

## src/jenkins/pipeline/notification/ (3 files)

### SlackNotifier.groovy
**Purpose:** Sends notifications to Slack via webhook.

### EmailNotifier.groovy
**Purpose:** Sends email notifications via Jenkins.

### NotificationService.groovy
**Purpose:** Aggregates multiple notification channels.

---

## src/jenkins/pipeline/distribution/ (1 file)

### PlayStoreDistributor.groovy
**Purpose:** Handles Google Play Store uploads and promotions.

---

## src/jenkins/pipeline/models/ (4 files)

### BuildConfig.groovy
**Purpose:** Configuration object for build operations.

**Properties:** appName, version, versionCode, buildType, environment, outputType

### AppVersion.groovy
**Purpose:** Handles semantic versioning.

### BuildResult.groovy
**Purpose:** Represents build output (success, artifact path).

### TestResult.groovy
**Purpose:** Represents test execution results.

---

## src/jenkins/pipeline/config/ (3 files)

### FastlaneConfig.groovy
**Purpose:** Configuration for Fastlane operations.

### AndroidConfig.groovy
**Purpose:** Android SDK paths and Gradle settings.

### PipelineConfig.groovy
**Purpose:** Central pipeline configuration.

---

## src/jenkins/pipeline/interfaces/ (4 files)

### IBuilder.groovy
**Purpose:** Contract that all builders must implement.

### ITestRunner.groovy
**Purpose:** Contract that all test runners must implement.

### IDistributor.groovy
**Purpose:** Contract for distribution targets.

### INotifier.groovy
**Purpose:** Contract for notification services.

---

## src/jenkins/pipeline/enums/ (5 files)

| File | Values | Purpose |
|------|--------|---------|
| `BuildType.groovy` | DEBUG, RELEASE | Build type selection |
| `OutputType.groovy` | APK, AAB | Output artifact type |
| `Platform.groovy` | ANDROID, IOS | Target platform |
| `Environment.groovy` | DEVELOPMENT, PRODUCTION | Target environment |
| `PlayStoreTrack.groovy` | INTERNAL, ALPHA, BETA, PRODUCTION | Play Store release track |

---

## src/jenkins/pipeline/exceptions/ (4 files)

| File | Purpose |
|------|---------|
| `PipelineException.groovy` | Base exception class |
| `BuildException.groovy` | Build failure errors |
| `DeployException.groovy` | Deployment errors |
| `TestException.groovy` | Test execution errors |

---

## src/jenkins/pipeline/utils/ (3 files)

### CommandBuilder.groovy
**Purpose:** Fluent builder for shell commands.

### FileUtils.groovy
**Purpose:** File operations (read, write, find, archive).

### GradleUtils.groovy
**Purpose:** Gradle-specific utilities.

---

## src/jenkins/pipeline/logging/ (1 file)

### PipelineLogger.groovy
**Purpose:** Colorized console output for Jenkins.

**Methods:** info(), success(), warn(), error(), stage(), section()

---

## src/jenkins/pipeline/secrets/ (1 file)

### SecretsManager.groovy
**Purpose:** Manages Jenkins credentials securely.

---

## src/jenkins/pipeline/validators/ (1 file)

### ConfigValidator.groovy
**Purpose:** Validates pipeline configuration.

---

## resources/templates/android/ (3 files)

| File | Purpose |
|------|---------|
| `Fastfile.template` | Fastlane lanes for build and deploy |
| `Appfile.template` | App package name and JSON key path |
| `Gemfile.template` | Ruby dependencies for Fastlane |
