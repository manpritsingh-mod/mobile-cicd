#!/usr/bin/env groovy

import jenkins.pipeline.models.BuildConfig
import jenkins.pipeline.mobile.AndroidBuilder
import jenkins.pipeline.logging.PipelineLogger

/**
 * Build Android app.
 * 
 * Usage:
 *   def result = androidBuild(appName: 'MyApp', buildType: 'release')
 */
def call(Map params = [:]) {
    PipelineLogger logger = PipelineLogger.create(this)
    
    // Create config
    BuildConfig config = new BuildConfig([
        appName: params.appName ?: env.APP_NAME ?: 'ReactNativeApp',
        version: params.version ?: getVersion(),
        versionCode: params.versionCode ?: (env.BUILD_NUMBER?.toInteger() ?: 1),
        buildType: params.buildType ?: 'debug',
        environment: params.environment ?: 'dev',
        outputType: params.outputType ?: 'apk',
        flavor: params.flavor,
        cleanBuild: params.cleanBuild != false
    ])
    
    // Build
    AndroidBuilder builder = new AndroidBuilder(this)
    
    // Validate
    List<String> errors = builder.validateEnvironment()
    if (errors) {
        errors.each { logger.error(it) }
        return [success: false, error: 'Environment validation failed']
    }
    
    // Install dependencies
    if (params.installDependencies != false) {
        builder.installDependencies()
    }
    
    // Run build
    Map result = builder.build(config)
    
    // Store in env for other stages
    if (result.success) {
        env.BUILD_ARTIFACT_PATH = result.artifactPath
        env.BUILD_VERSION = result.version
    }
    
    return result
}

/**
 * Get version from package.json
 */
private String getVersion() {
    if (fileExists('package.json')) {
        def pkg = readJSON(file: 'package.json')
        return pkg.version ?: '1.0.0'
    }
    return '1.0.0'
}
