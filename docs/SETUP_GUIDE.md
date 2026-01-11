# Quick Setup Guide

## Prerequisites

- Jenkins with Pipeline plugin
- Docker installed on Jenkins agents
- Access to Nexus registry (43.88.89.25:5000)
- Google Play Console service account

---

## Step 1: Build & Push Docker Image

```bash
cd CI_CD\ Mobile
docker build -t 43.88.89.25:5000/react-native-android:latest .
docker push 43.88.89.25:5000/react-native-android:latest
```

---

## Step 2: Add Shared Library to Jenkins

**Manage Jenkins → Configure System → Global Pipeline Libraries**

| Setting | Value |
|---------|-------|
| Name | `mobile-ci-cd` |
| Default version | `main` |
| Allow default version override | ✓ |
| Retrieval method | Modern SCM |
| Source Code Management | Git |
| Project Repository | `<your-git-repo-url>` |

---

## Step 3: Add Jenkins Credentials

**Manage Jenkins → Manage Credentials → Add Credentials**

| ID | Type | Description |
|----|------|-------------|
| `android-keystore` | Secret file | Upload your .jks keystore file |
| `android-keystore-password` | Secret text | Your keystore password |
| `google-play-json-key` | Secret file | Service account JSON from Play Console |
| `slack-webhook` | Secret text | Slack incoming webhook URL |

---

## Step 4: Create Jenkinsfile in Your Project

```groovy
@Library('mobile-ci-cd') _

androidPipeline(
    appName: 'MyApp',
    keystoreCredentialsId: 'android-keystore',
    keystorePasswordCredentialsId: 'android-keystore-password',
    playStoreCredentialsId: 'google-play-json-key',
    slackChannel: '#mobile-builds'
)
```

---

## Step 5: Run Pipeline

1. Create new Pipeline job in Jenkins
2. Point to your project's Git repository
3. Jenkins will automatically use the Jenkinsfile
4. Build parameters:
   - `BUILD_TYPE`: debug / release
   - `ENVIRONMENT`: dev / prod
   - `DEPLOY`: true / false

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Docker pull fails | Check Nexus registry credentials |
| Build fails | Check ANDROID_HOME is set |
| Deploy fails | Verify Play Store JSON key is valid |
| Tests fail | Check Jest/Appium configuration |
