/**
 * Example Jenkinsfile using the mobile-ci-cd shared library.
 * 
 * Prerequisites:
 * 1. Configure Jenkins with this shared library under:
 *    Manage Jenkins > Configure System > Global Pipeline Libraries
 *    - Name: mobile-ci-cd
 *    - Default version: main
 *    - Retrieval method: Modern SCM (Git)
 * 
 * 2. Configure required credentials:
 *    - git-credentials: Git access token/SSH key
 *    - android-keystore: Android signing keystore file
 *    - android-keystore-password: Keystore password
 *    - google-play-json-key: Google Play Service Account JSON
 *    - slack-webhook: Slack webhook URL
 */

@Library('mobile-ci-cd') _

// ============================================================================
// Simple Usage (Recommended for most projects)
// ============================================================================

androidPipeline(
    // App Configuration
    appName: 'MyReactNativeApp',
    
    // Git Configuration
    gitBranch: 'main',
    gitCredentialsId: 'git-credentials',
    
    // Docker Configuration (Nexus registry)
    dockerRegistry: '43.88.89.25:5000',
    dockerImage: 'react-native-android',
    dockerTag: 'latest',
    
    // Build Configuration
    buildType: 'release',          // 'debug' or 'release'
    environment: 'prod',           // 'dev', 'staging', or 'prod'
    
    // Signing Configuration
    keystoreCredentialsId: 'android-keystore',
    keystorePasswordCredentialsId: 'android-keystore-password',
    keyAlias: 'release',
    
    // Deployment Configuration
    playStoreCredentialsId: 'google-play-json-key',
    playStoreTrack: 'internal',    // 'internal', 'alpha', 'beta', 'production'
    rolloutPercentage: 100,
    
    // Notification Configuration
    slackWebhookCredentialsId: 'slack-webhook',
    slackChannel: '#mobile-builds',
    emailRecipients: ['dev-team@company.com'],
    
    // Testing Configuration
    runUnitTests: true,
    runE2ETests: false,
    emulatorUrl: 'http://emulator-host:5555',
    
    // Timeouts
    buildTimeout: 30,
    testTimeout: 20
)

// ============================================================================
// Advanced Usage (Custom Pipeline with Stages)
// ============================================================================
/*
@Library('mobile-ci-cd') _

pipeline {
    agent {
        docker {
            image '43.88.89.25:5000/react-native-android:latest'
            args '--network=host -u root'
        }
    }
    
    parameters {
        choice(name: 'BUILD_TYPE', choices: ['debug', 'release'], description: 'Build type')
        choice(name: 'ENVIRONMENT', choices: ['dev', 'prod'], description: 'Environment')
        booleanParam(name: 'DEPLOY', defaultValue: false, description: 'Deploy to Play Store')
    }
    
    environment {
        APP_NAME = 'MyApp'
        ANDROID_HOME = '/home/jenkins/android-sdk'
    }
    
    stages {
        stage('Checkout') {
            steps {
                gitCheckout(branch: 'main')
            }
        }
        
        stage('Setup') {
            steps {
                setupEnv(nodeVersion: '20')
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh 'yarn install --frozen-lockfile'
            }
        }
        
        stage('Lint') {
            steps {
                sh 'npm run lint'
            }
        }
        
        stage('Unit Tests') {
            steps {
                script {
                    def result = androidTest(framework: 'jest')
                    notifyBuild(type: 'test', result: result)
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    withAndroidSigning(
                        keystoreCredentialsId: 'android-keystore',
                        passwordCredentialsId: 'android-keystore-password'
                    ) {
                        buildResult = androidBuild(
                            appName: env.APP_NAME,
                            buildType: params.BUILD_TYPE,
                            environment: params.ENVIRONMENT,
                            outputType: params.DEPLOY ? 'aab' : 'apk'
                        )
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '** /*.apk, ** /*.aab', allowEmptyArchive: true
                }
            }
        }
        
        stage('Deploy') {
            when {
                expression { params.DEPLOY && buildResult?.success }
            }
            steps {
                script {
                    withCredentials([file(credentialsId: 'google-play-json-key', variable: 'GOOGLE_PLAY_JSON_KEY')]) {
                        androidDeploy(
                            artifactPath: buildResult.artifactPath,
                            track: 'internal'
                        )
                    }
                }
            }
        }
    }
    
    post {
        success {
            notifyBuild(type: 'success', result: buildResult)
        }
        failure {
            notifyBuild(type: 'failure', error: currentBuild.description)
        }
        always {
            cleanWs()
        }
    }
}
*/
