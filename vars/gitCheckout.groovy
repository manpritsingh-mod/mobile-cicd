#!/usr/bin/env groovy

import jenkins.pipeline.logging.PipelineLogger

/**
 * Git checkout with configuration options.
 * 
 * Usage:
 * ```groovy
 * gitCheckout(
 *     branch: 'main',
 *     credentialsId: 'git-credentials'
 * )
 * ```
 */
def call(Map params = [:]) {
    PipelineLogger logger = PipelineLogger.create(this)
    logger.stage('Git Checkout')
    
    String branch = params.branch ?: env.GIT_BRANCH ?: 'main'
    String credentialsId = params.credentialsId ?: 'git-credentials'
    String url = params.url ?: env.GIT_URL
    
    // Clean workspace first if requested
    if (params.clean) {
        logger.info("Cleaning workspace...")
        deleteDir()
    }
    
    logger.property("Branch", branch)
    logger.property("URL", url ?: 'From SCM')
    
    if (url) {
        // Explicit checkout
        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/${branch}"]],
            extensions: [
                [$class: 'CleanBeforeCheckout'],
                [$class: 'CloneOption', 
                 depth: params.depth ?: 0,
                 noTags: params.noTags ?: false,
                 shallow: params.shallow ?: false
                ],
                [$class: 'SubmoduleOption',
                 disableSubmodules: params.disableSubmodules ?: false,
                 recursiveSubmodules: params.recursiveSubmodules ?: true
                ]
            ],
            userRemoteConfigs: [[
                url: url,
                credentialsId: credentialsId
            ]]
        ])
    } else {
        // Use default SCM from job config
        checkout scm
    }
    
    // Get commit info
    String commitHash = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
    String commitMessage = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
    String commitAuthor = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
    
    logger.property("Commit", commitHash.take(8))
    logger.property("Author", commitAuthor)
    logger.property("Message", commitMessage.take(50))
    
    // Store commit info in environment
    env.GIT_COMMIT_SHORT = commitHash.take(8)
    env.GIT_COMMIT_MESSAGE = commitMessage.take(100)
    env.GIT_COMMIT_AUTHOR = commitAuthor
    
    logger.success("Checkout completed")
    
    return [
        commit: commitHash,
        shortCommit: commitHash.take(8),
        message: commitMessage,
        author: commitAuthor,
        branch: branch
    ]
}

/**
 * Checkout a specific tag.
 */
def tag(String tagName, Map params = [:]) {
    params.branch = "refs/tags/${tagName}"
    return call(params)
}

/**
 * Checkout a specific commit.
 */
def commit(String commitHash, Map params = [:]) {
    params.branch = commitHash
    return call(params)
}
