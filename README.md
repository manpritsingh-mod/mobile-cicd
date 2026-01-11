# Mobile CI/CD Jenkins Shared Library

Jenkins shared library for React Native Android CI/CD with Fastlane and Play Store deployment.

## Quick Start

```groovy
@Library('mobile-ci-cd') _

androidPipeline(
    appName: 'MyApp',
    dockerRegistry: '43.88.89.25:5000',
    dockerImage: 'react-native-android'
)
```

## Setup

### 1. Build & Push Docker Image

```bash
docker build -t 43.88.89.25:5000/react-native-android:latest .
docker push 43.88.89.25:5000/react-native-android:latest
```

### 2. Add Library to Jenkins

**Manage Jenkins > Configure System > Global Pipeline Libraries**

| Field | Value |
|-------|-------|
| Name | `mobile-ci-cd` |
| Default version | `main` |
| Source | Git URL of this repo |

### 3. Configure Credentials

| ID | Type | Description |
|----|------|-------------|
| `android-keystore` | Secret file | Signing keystore (.jks) |
| `android-keystore-password` | Secret text | Keystore password |
| `google-play-json-key` | Secret file | Play Store service account JSON |
| `slack-webhook` | Secret text | Slack webhook URL (optional) |

## Available Functions

| Function | Description |
|----------|-------------|
| `androidPipeline()` | Complete CI/CD pipeline |
| `androidBuild()` | Build APK/AAB |
| `androidTest()` | Run Jest/Appium tests |
| `androidDeploy()` | Deploy to Play Store |
| `withAndroidSigning()` | Configure signing |
| `notifyBuild()` | Send Slack notifications |

## Project Structure

```
├── src/jenkins/pipeline/   # Core classes
├── vars/                   # Pipeline functions
├── resources/templates/    # Fastlane templates
├── Dockerfile             # Build agent image
└── Jenkinsfile.example    # Usage example
```
