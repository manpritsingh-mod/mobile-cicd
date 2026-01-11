# Folder Structure

## Complete Directory Tree

```
CI_CD Mobile/
├── Dockerfile                          # Docker build agent image
├── README.md                           # Quick start guide
│
├── docs/                               # Documentation (YOU ARE HERE)
│   ├── ARCHITECTURE.md                 # System architecture diagrams
│   ├── PIPELINE_FLOW.md                # Pipeline stage flow
│   ├── FOLDER_STRUCTURE.md             # This file
│   ├── API_REFERENCE.md                # All APIs and methods
│   └── IMPLEMENTATION_PLAN.md          # How it was built
│
├── vars/                               # Pipeline Entry Points (Global Functions)
│   ├── androidPipeline.groovy          # Main pipeline orchestrator
│   ├── androidBuild.groovy             # Build APK/AAB
│   ├── androidTest.groovy              # Run Jest/Appium tests
│   ├── androidDeploy.groovy            # Deploy to Play Store
│   ├── notifyBuild.groovy              # Slack notifications
│   ├── withAndroidSigning.groovy       # Signing credentials wrapper
│   ├── gitCheckout.groovy              # Git operations
│   └── setupEnv.groovy                 # Environment setup
│
├── src/jenkins/pipeline/               # Core Library Classes
│   │
│   ├── mobile/                         # Mobile Builders
│   │   ├── AndroidBuilder.groovy       # Android build logic
│   │   └── BuilderFactory.groovy       # Factory for builders
│   │
│   ├── testing/                        # Test Runners
│   │   ├── JestRunner.groovy           # Jest unit tests
│   │   ├── AppiumRunner.groovy         # Appium E2E tests
│   │   └── TestOrchestrator.groovy     # Test coordination
│   │
│   ├── ci/                             # CI/CD Tools
│   │   ├── FastlaneExecutor.groovy     # Fastlane commands
│   │   └── DockerImageManager.groovy   # Docker operations
│   │
│   ├── notification/                   # Notification Services
│   │   ├── SlackNotifier.groovy        # Slack notifications
│   │   ├── EmailNotifier.groovy        # Email notifications
│   │   └── NotificationService.groovy  # Notification aggregator
│   │
│   ├── distribution/                   # App Distribution
│   │   └── PlayStoreDistributor.groovy # Google Play Store
│   │
│   ├── models/                         # Data Models
│   │   ├── BuildConfig.groovy          # Build configuration
│   │   ├── AppVersion.groovy           # Version handling
│   │   ├── BuildResult.groovy          # Build output
│   │   └── TestResult.groovy           # Test output
│   │
│   ├── config/                         # Configuration Classes
│   │   ├── FastlaneConfig.groovy       # Fastlane settings
│   │   ├── AndroidConfig.groovy        # Android SDK settings
│   │   └── PipelineConfig.groovy       # Pipeline settings
│   │
│   ├── interfaces/                     # Contracts/Interfaces
│   │   ├── IBuilder.groovy             # Builder interface
│   │   ├── ITestRunner.groovy          # Test runner interface
│   │   ├── IDistributor.groovy         # Distributor interface
│   │   └── INotifier.groovy            # Notifier interface
│   │
│   ├── enums/                          # Enumerations
│   │   ├── BuildType.groovy            # debug/release
│   │   ├── OutputType.groovy           # apk/aab
│   │   ├── Platform.groovy             # android/ios
│   │   ├── Environment.groovy          # dev/prod
│   │   └── PlayStoreTrack.groovy       # internal/alpha/beta/prod
│   │
│   ├── exceptions/                     # Custom Exceptions
│   │   ├── PipelineException.groovy    # Base exception
│   │   ├── BuildException.groovy       # Build errors
│   │   ├── DeployException.groovy      # Deploy errors
│   │   └── TestException.groovy        # Test errors
│   │
│   ├── utils/                          # Utilities
│   │   ├── CommandBuilder.groovy       # Shell command builder
│   │   ├── FileUtils.groovy            # File operations
│   │   └── GradleUtils.groovy          # Gradle utilities
│   │
│   ├── logging/                        # Logging
│   │   └── PipelineLogger.groovy       # Colored console output
│   │
│   ├── secrets/                        # Credentials
│   │   └── SecretsManager.groovy       # Jenkins credentials
│   │
│   └── validators/                     # Validation
│       └── ConfigValidator.groovy      # Config validation
│
└── resources/templates/android/        # Fastlane Templates
    ├── Fastfile.template               # Fastlane lanes
    ├── Appfile.template                # App configuration
    └── Gemfile.template                # Ruby dependencies
```

## Folder Purpose Explanation

| Folder | Purpose | When Used |
|--------|---------|-----------|
| `vars/` | Entry points called from Jenkinsfile | Every pipeline run |
| `src/jenkins/pipeline/mobile/` | Build logic for Android | Build stage |
| `src/jenkins/pipeline/testing/` | Test execution | Test stages |
| `src/jenkins/pipeline/ci/` | CI tools (Fastlane, Docker) | Build & Deploy |
| `src/jenkins/pipeline/notification/` | Notifications | Success/Failure |
| `src/jenkins/pipeline/distribution/` | App store upload | Deploy stage |
| `src/jenkins/pipeline/models/` | Data structures | Throughout |
| `src/jenkins/pipeline/config/` | Configuration | Setup |
| `src/jenkins/pipeline/interfaces/` | Contracts | Design pattern |
| `src/jenkins/pipeline/enums/` | Constants | Throughout |
| `src/jenkins/pipeline/exceptions/` | Error handling | Error cases |
| `src/jenkins/pipeline/utils/` | Helper functions | Throughout |
| `resources/templates/` | Fastlane files | Deploy stage |
