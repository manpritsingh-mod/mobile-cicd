# API Reference

## vars/ Functions

### androidPipeline()

Main entry point for the complete CI/CD pipeline.

```groovy
androidPipeline(
    appName: 'MyApp',                              // Required: App name
    dockerRegistry: '43.88.89.25:5000',            // Docker registry URL
    dockerImage: 'react-native-android',           // Docker image name
    dockerTag: 'latest',                           // Docker image tag
    gitBranch: 'main',                             // Git branch
    keystoreCredentialsId: 'android-keystore',     // Keystore credential ID
    keystorePasswordCredentialsId: 'keystore-pwd', // Password credential ID
    keyAlias: 'release',                           // Key alias
    playStoreCredentialsId: 'google-play-json',    // Play Store JSON key
    playStoreTrack: 'internal',                    // internal/alpha/beta/production
    slackChannel: '#builds',                       // Slack channel
    runUnitTests: true,                            // Run Jest tests
    runE2ETests: false,                            // Run Appium tests
    emulatorUrl: 'http://emulator:5555',           // Appium emulator URL
    buildTimeout: 30                               // Timeout in minutes
)
```

---

### androidBuild()

Build Android APK or AAB.

```groovy
def result = androidBuild(
    appName: 'MyApp',           // App name
    version: '1.0.0',           // Version string
    versionCode: 1,             // Android version code
    buildType: 'release',       // debug / release
    environment: 'prod',        // dev / prod
    outputType: 'aab',          // apk / aab
    flavor: null,               // Optional product flavor
    cleanBuild: true,           // Run clean first
    installDependencies: true   // Run npm/yarn install
)

// Returns:
// {
//   success: true/false,
//   artifactPath: '/path/to/app.aab',
//   version: '1.0.0',
//   versionCode: 1,
//   duration: 120000,
//   error: null
// }
```

---

### androidTest()

Run unit tests (Jest) or E2E tests (Appium).

```groovy
// Jest Unit Tests
def result = androidTest(
    framework: 'jest',     // jest / appium
    coverage: true         // Generate coverage report
)

// Appium E2E Tests
def result = androidTest(
    framework: 'appium',
    emulatorUrl: 'http://emulator:5555',
    appPath: '/path/to/app.apk'
)

// Returns:
// {
//   success: true/false,
//   framework: 'Jest',
//   passed: 42,
//   failed: 0,
//   total: 42,
//   duration: 15000
// }
```

---

### androidDeploy()

Deploy to Google Play Store.

```groovy
androidDeploy(
    artifactPath: '/path/to/app.aab',  // Path to AAB file
    track: 'internal',                  // internal/alpha/beta/production
    rolloutPercentage: 100              // Rollout percentage (1-100)
)

// Returns:
// {
//   success: true/false,
//   track: 'internal'
// }
```

---

### withAndroidSigning()

Wrapper for signing credentials.

```groovy
withAndroidSigning(
    keystoreCredentialsId: 'android-keystore',
    passwordCredentialsId: 'keystore-password',
    keyAlias: 'release'
) {
    // Your code here has access to signing env vars:
    // ANDROID_KEYSTORE_PATH
    // ANDROID_KEYSTORE_PASSWORD
    // ANDROID_KEY_ALIAS
}
```

---

### notifyBuild()

Send Slack notifications.

```groovy
// Build started
notifyBuild(type: 'started', channel: '#builds')

// Build success
notifyBuild(type: 'success', channel: '#builds')

// Build failure
notifyBuild(type: 'failure', channel: '#builds', error: 'Build failed')

// Custom message
notifyBuild(message: 'Custom notification', channel: '#builds')
```

---

## Core Classes

### AndroidBuilder

```groovy
class AndroidBuilder implements IBuilder {
    
    // Constructor
    AndroidBuilder(Object script)
    
    // Methods
    Map build(BuildConfig config)      // Build the app
    boolean clean()                     // Clean build directory
    boolean installDependencies()       // npm/yarn install
    boolean runLint()                   // Run ESLint
    List<String> validateEnvironment()  // Check ANDROID_HOME, etc.
}
```

---

### JestRunner

```groovy
class JestRunner implements ITestRunner {
    
    // Constructor
    JestRunner(Object script, boolean coverage = true)
    
    // Methods
    Map runTests()           // Run Jest tests
    boolean isAvailable()    // Check if Jest is configured
    String getFrameworkName() // Returns 'Jest'
}
```

---

### AppiumRunner

```groovy
class AppiumRunner implements ITestRunner {
    
    // Constructor
    AppiumRunner(Object script, String emulatorUrl)
    
    // Methods
    AppiumRunner withApp(String path)  // Set APK path
    Map runTests()                      // Run Appium tests
    boolean isAvailable()               // Check if wdio.conf.js exists
    String getFrameworkName()           // Returns 'Appium'
}
```

---

### FastlaneExecutor

```groovy
class FastlaneExecutor {
    
    // Constructor
    FastlaneExecutor(Object script, String fastlaneDir, String packageName)
    
    // Methods
    int runLane(String lane, Map options)  // Run any Fastlane lane
    boolean deployToPlayStore(             // Deploy to Play Store
        String aabPath, 
        String track, 
        double rollout
    )
}
```

---

### BuildConfig

```groovy
class BuildConfig {
    
    // Properties
    String appName
    String version
    int versionCode
    String buildType      // debug / release
    String environment    // dev / prod
    String outputType     // apk / aab
    String flavor
    boolean cleanBuild
    
    // Constructor
    BuildConfig(Map config)
    
    // Methods
    String getVariantName()    // e.g., "devDebug"
    String getGradleTask()     // e.g., "assembleDevDebug"
    boolean isRelease()        // Check if release build
}
```

---

### PipelineLogger

```groovy
class PipelineLogger {
    
    // Constructor
    static PipelineLogger create(Object script)
    
    // Methods
    void info(String message)      // Blue text
    void success(String message)   // Green text
    void warn(String message)      // Yellow text
    void error(String message)     // Red text
    void stage(String name)        // Stage header
    void section(String name)      // Section header
    void property(String k, String v) // Key-value pair
}
```
