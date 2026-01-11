#!/usr/bin/env groovy

/**
 * Simple Android CI/CD Pipeline.
 * 
 * Usage:
 *   androidPipeline(
 *       appName: 'MyApp',
 *       dockerImage: 'react-native-android'
 *   )
 */
def call(Map config = [:]) {
    
    // Defaults
    String appName = config.appName ?: 'ReactNativeApp'
    String dockerRegistry = config.dockerRegistry ?: '43.88.89.25:5000'
    String dockerImage = config.dockerImage ?: 'react-native-android'
    String dockerTag = config.dockerTag ?: 'latest'
    String gitBranch = config.gitBranch ?: 'main'
    
    def buildResult = null
    
    pipeline {
        agent {
            docker {
                image "${dockerRegistry}/${dockerImage}:${dockerTag}" // 43.88.89.25:5000/react-native-android:latest
                args '--network=host -u root'
            }
        }
        
        options {
            timeout(time: config.buildTimeout ?: 30, unit: 'MINUTES')
            timestamps()
            ansiColor('xterm')
        }
        
        parameters {
            choice(name: 'BUILD_TYPE', choices: ['debug', 'release'], description: 'Build type')
            choice(name: 'ENVIRONMENT', choices: ['dev', 'prod'], description: 'Environment')
            booleanParam(name: 'DEPLOY', defaultValue: false, description: 'Deploy to Play Store')
        }
        
        environment {
            ANDROID_HOME = '/home/jenkins/android-sdk'
            ANDROID_SDK_ROOT = '/home/jenkins/android-sdk'
        }
        
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                    echo "Checked out ${gitBranch}"
                }
            }
            
            stage('Setup') {
                steps {
                    sh 'chmod +x android/gradlew'
                    sh 'node --version && npm --version'
                }
            }
            
            stage('Install Dependencies') {
                steps {
                    script {
                        if (fileExists('yarn.lock')) {
                            sh 'yarn install --frozen-lockfile'
                        } else {
                            sh 'npm ci'
                        }
                    }
                }
            }
            
            stage('Lint') {
                steps {
                    sh 'npm run lint || true'
                }
            }
            
            stage('Build') {
                steps {
                    script {
                        String outputType = params.DEPLOY ? 'aab' : 'apk'
                        
                        // Use signing for release
                        if (params.BUILD_TYPE == 'release' && config.keystoreCredentialsId) {
                            withAndroidSigning(
                                keystoreCredentialsId: config.keystoreCredentialsId,
                                passwordCredentialsId: config.keystorePasswordCredentialsId,
                                keyAlias: config.keyAlias ?: 'release'
                            ) {
                                buildResult = androidBuild(
                                    appName: appName,
                                    buildType: params.BUILD_TYPE,
                                    environment: params.ENVIRONMENT,
                                    outputType: outputType
                                )
                            }
                        } else {
                            buildResult = androidBuild(
                                appName: appName,
                                buildType: params.BUILD_TYPE,
                                environment: params.ENVIRONMENT,
                                outputType: outputType
                            )
                        }
                        
                        if (!buildResult.success) {
                            error("Build failed: ${buildResult.error}")
                        }
                    }
                }
                post {
                    success {
                        archiveArtifacts artifacts: '**/*.apk, **/*.aab', allowEmptyArchive: true
                    }
                }
            }

            stage('Unit Tests') {
                when { expression { config.runUnitTests != false } }
                steps {
                    script {
                        def result = androidTest(framework: 'jest')
                        if (result.success) {
                            echo "Unit Tests: ${result.passed}/${result.total} passed"
                        } else {
                            error("Unit tests failed")
                        }
                    }
                }
            }
            
            stage('E2E Tests') {
                when { expression { config.runE2ETests == true } }
                steps {
                    script {
                        def result = androidTest(
                            framework: 'appium',
                            emulatorUrl: config.emulatorUrl ?: env.EMULATOR_URL,
                            appPath: buildResult?.artifactPath
                        )
                        if (result.success) {
                            echo "E2E Tests passed"
                        } else {
                            error("E2E tests failed")
                        }
                    }
                }
            }
            
            stage('Deploy') {
                when {
                    expression { params.DEPLOY && buildResult?.success }
                }
                steps {
                    script {
                        withCredentials([file(credentialsId: config.playStoreCredentialsId ?: 'google-play-json-key', variable: 'GOOGLE_PLAY_JSON_KEY')]) {
                            androidDeploy(
                                artifactPath: buildResult.artifactPath,
                                track: config.playStoreTrack ?: 'internal'
                            )
                        }
                    }
                }
            }
        }
        
        post {
            success {
                echo "Build succeeded!"
                script {
                    if (config.slackChannel) {
                        notifyBuild(type: 'success', channel: config.slackChannel)
                    }
                }
            }
            failure {
                echo "Build failed!"
                script {
                    if (config.slackChannel) {
                        notifyBuild(type: 'failure', channel: config.slackChannel)
                    }
                }
            }
            always {
                cleanWs()
            }
        }
    }
}
