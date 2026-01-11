#!/usr/bin/env groovy

import jenkins.pipeline.logging.PipelineLogger

/**
 * Setup build environment.
 * 
 * Usage:
 * ```groovy
 * setupEnv(
 *     nodeVersion: '20',
 *     javaVersion: '17'
 * )
 * ```
 */
def call(Map params = [:]) {
    PipelineLogger logger = PipelineLogger.create(this)
    logger.stage('Setup Environment')
    
    // Validate environment
    validateAndroidSdk(logger)
    
    // Setup Node.js
    String nodeVersion = params.nodeVersion ?: '20'
    setupNode(nodeVersion, logger)
    
    // Setup Gradle
    setupGradle(logger)
    
    // Setup React Native
    setupReactNative(logger)
    
    // Log environment info
    logEnvironmentInfo(logger)
    
    logger.success("Environment setup completed")
}

/**
 * Validate Android SDK installation.
 */
private void validateAndroidSdk(PipelineLogger logger) {
    logger.info("Validating Android SDK...")
    
    String androidHome = env.ANDROID_HOME ?: env.ANDROID_SDK_ROOT
    
    if (!androidHome) {
        logger.warn("ANDROID_HOME not set, attempting to detect...")
        
        // Try common paths
        List<String> possiblePaths = [
            '/home/jenkins/android-sdk',
            '/opt/android-sdk',
            '/usr/local/android-sdk'
        ]
        
        for (String path : possiblePaths) {
            if (fileExists("${path}/platform-tools/adb")) {
                env.ANDROID_HOME = path
                env.ANDROID_SDK_ROOT = path
                logger.info("Found Android SDK at ${path}")
                break
            }
        }
    }
    
    // Verify SDK components
    int sdkCheck = sh(
        script: 'sdkmanager --list 2>/dev/null | head -5',
        returnStatus: true
    )
    
    if (sdkCheck == 0) {
        logger.success("Android SDK validated")
    } else {
        logger.warn("SDK manager not available or not in PATH")
    }
}

/**
 * Setup Node.js environment.
 */
private void setupNode(String version, PipelineLogger logger) {
    logger.info("Setting up Node.js v${version}...")
    
    // Check Node version
    String currentVersion = sh(
        script: 'node --version 2>/dev/null || echo "not installed"',
        returnStdout: true
    ).trim()
    
    logger.property("Node.js", currentVersion)
    
    // Check npm
    String npmVersion = sh(
        script: 'npm --version 2>/dev/null || echo "not installed"',
        returnStdout: true
    ).trim()
    
    logger.property("npm", npmVersion)
    
    // Check yarn
    String yarnVersion = sh(
        script: 'yarn --version 2>/dev/null || echo "not installed"',
        returnStdout: true
    ).trim()
    
    logger.property("Yarn", yarnVersion)
    
    // Set npm cache location
    env.NPM_CONFIG_CACHE = "${env.WORKSPACE}/.npm"
    sh "mkdir -p ${env.WORKSPACE}/.npm"
}

/**
 * Setup Gradle environment.
 */
private void setupGradle(PipelineLogger logger) {
    logger.info("Setting up Gradle...")
    
    // Set Gradle home
    env.GRADLE_USER_HOME = "${env.WORKSPACE}/.gradle"
    sh "mkdir -p ${env.WORKSPACE}/.gradle"
    
    // Make gradlew executable if exists
    if (fileExists('android/gradlew')) {
        sh 'chmod +x android/gradlew'
        
        String gradleVersion = sh(
            script: 'cd android && ./gradlew --version 2>/dev/null | grep "Gradle " || echo "unknown"',
            returnStdout: true
        ).trim()
        
        logger.property("Gradle", gradleVersion)
    }
    
    // Configure Gradle properties for CI
    String gradleProps = """
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
org.gradle.console=plain
"""
    
    writeFile(file: "${env.WORKSPACE}/.gradle/gradle.properties", text: gradleProps)
}

/**
 * Setup React Native environment.
 */
private void setupReactNative(PipelineLogger logger) {
    logger.info("Setting up React Native...")
    
    // Check React Native version
    if (fileExists('package.json')) {
        def packageJson = readJSON(file: 'package.json')
        String rnVersion = packageJson.dependencies?.'react-native' ?: 'not found'
        logger.property("React Native", rnVersion)
    }
    
    // Set environment for Metro bundler
    env.DISABLE_DEV_MENU_ATTACH = 'true'
}

/**
 * Log current environment information.
 */
private void logEnvironmentInfo(PipelineLogger logger) {
    logger.section("Environment Info")
    
    String javaVersion = sh(
        script: 'java -version 2>&1 | head -1',
        returnStdout: true
    ).trim()
    
    logger.property("Java", javaVersion)
    logger.property("ANDROID_HOME", env.ANDROID_HOME ?: 'not set')
    logger.property("GRADLE_USER_HOME", env.GRADLE_USER_HOME)
    logger.property("Workspace", env.WORKSPACE)
}

/**
 * Install a specific tool.
 */
def installTool(String tool, Map params = [:]) {
    PipelineLogger logger = PipelineLogger.create(this)
    
    switch (tool.toLowerCase()) {
        case 'bundletool':
            String version = params.version ?: '1.15.6'
            sh """
                curl -L "https://github.com/google/bundletool/releases/download/${version}/bundletool-all-${version}.jar" \
                    -o bundletool.jar
                chmod +x bundletool.jar
            """
            logger.success("BundleTool ${version} installed")
            break
            
        case 'fastlane':
            sh 'gem install fastlane'
            logger.success("Fastlane installed")
            break
            
        default:
            logger.warn("Unknown tool: ${tool}")
    }
}
