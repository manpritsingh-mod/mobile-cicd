# CI/CD Pipeline Flow

## Complete Pipeline Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PIPELINE STAGES                                │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │  START   │
    └────┬─────┘
         │
         ▼
┌──────────────────┐
│   1. CHECKOUT    │  Pull code from Git repository
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│    2. SETUP      │  Make gradlew executable, check Node/npm versions
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ 3. DEPENDENCIES  │  yarn install OR npm ci
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│     4. LINT      │  npm run lint (ESLint for JavaScript)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│    5. BUILD      │  ./gradlew assembleDevDebug OR bundleProdRelease
└────────┬─────────┘
         │
         ├─────────────────────────────────────┐
         ▼                                     ▼
┌──────────────────┐                 ┌──────────────────┐
│ 6. UNIT TESTS    │                 │  Archive APK/AAB │
│    (Jest)        │                 └──────────────────┘
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  7. E2E TESTS    │  (Optional) Appium with Docker Emulator
│    (Appium)      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│   8. DEPLOY      │  (If DEPLOY=true) Fastlane → Google Play Store
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│   9. NOTIFY      │  Slack notification (success/failure)
└────────┬─────────┘
         │
         ▼
    ┌──────────┐
    │   END    │
    └──────────┘
```

## Build Type Decision Flow

```
                    ┌─────────────────┐
                    │ BUILD_TYPE?     │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
       ┌────────────┐                ┌────────────┐
       │   DEBUG    │                │  RELEASE   │
       └──────┬─────┘                └──────┬─────┘
              │                             │
              ▼                             ▼
       ┌────────────┐                ┌────────────┐
       │ No Signing │                │  Signing   │
       │  Required  │                │  Required  │
       └──────┬─────┘                └──────┬─────┘
              │                             │
              ▼                             ▼
       ┌────────────┐                ┌────────────┐
       │ APK Output │                │ AAB Output │
       │ (Testing)  │                │ (Deploy)   │
       └────────────┘                └────────────┘
```

## Environment & Flavor Matrix

```
┌─────────────────────────────────────────────────────────────────┐
│                    BUILD VARIANTS                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│         ENVIRONMENT                                             │
│         ┌────────────────────────────────────┐                 │
│         │          dev           prod        │                 │
│         ├────────────────────────────────────┤                 │
│  BUILD  │                                    │                 │
│  TYPE   │   debug   devDebug    prodDebug   │                 │
│         │                                    │                 │
│         │   release devRelease  prodRelease │ ← Play Store    │
│         └────────────────────────────────────┘                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Deployment Track Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                 GOOGLE PLAY STORE TRACKS                        │
└─────────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │   AAB    │
    └────┬─────┘
         │
         ▼
┌──────────────────┐    Promote    ┌──────────────────┐
│    INTERNAL      │──────────────►│     ALPHA        │
│  (100 testers)   │               │  (Closed Testing)│
└──────────────────┘               └────────┬─────────┘
                                            │ Promote
                                            ▼
                                   ┌──────────────────┐
                                   │      BETA        │
                                   │  (Open Testing)  │
                                   └────────┬─────────┘
                                            │ Promote
                                            ▼
                                   ┌──────────────────┐
                                   │   PRODUCTION     │
                                   │  (All Users)     │
                                   └──────────────────┘
```
