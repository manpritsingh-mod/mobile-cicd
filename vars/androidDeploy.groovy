#!/usr/bin/env groovy

import jenkins.pipeline.ci.FastlaneExecutor
import jenkins.pipeline.logging.PipelineLogger

/**
 * Deploy to Google Play Store.
 * 
 * Usage:
 *   androidDeploy(artifactPath: 'app.aab', track: 'internal')
 */
def call(Map params = [:]) {
    PipelineLogger logger = PipelineLogger.create(this)
    
    // Get artifact
    String artifactPath = params.artifactPath ?: env.BUILD_ARTIFACT_PATH
    if (!artifactPath) {
        error("artifactPath is required")
    }
    
    if (!fileExists(artifactPath)) {
        error("Artifact not found: ${artifactPath}")
    }
    
    // Deploy
    String track = params.track ?: 'internal'
    double rollout = params.rolloutPercentage ?: 100.0
    
    FastlaneExecutor fastlane = new FastlaneExecutor(this)
    boolean success = fastlane.deployToPlayStore(artifactPath, track, rollout)
    
    if (success) {
        env.DEPLOY_TRACK = track
        return [success: true, track: track]
    } else {
        error("Deployment failed")
    }
}
