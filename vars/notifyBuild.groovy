#!/usr/bin/env groovy

/**
 * Send build notifications to Slack.
 * 
 * Usage:
 *   notifyBuild(type: 'success', channel: '#builds')
 *   notifyBuild(type: 'failure', channel: '#builds', error: 'Build failed')
 */
def call(Map params = [:]) {
    String type = params.type ?: 'info'
    String channel = params.channel ?: '#builds'
    String webhookUrl = params.webhookUrl ?: env.SLACK_WEBHOOK_URL
    
    if (!webhookUrl) {
        echo "Slack webhook not configured, skipping notification"
        return
    }
    
    // Build message
    String message
    String color
    
    switch (type) {
        case 'started':
            message = "🚀 Build started: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            color = '#439FE0'
            break
        case 'success':
            message = "✅ Build succeeded: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            color = 'good'
            break
        case 'failure':
            message = "❌ Build failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            if (params.error) message += "\nError: ${params.error}"
            color = 'danger'
            break
        default:
            message = params.message ?: "Build notification"
            color = '#439FE0'
    }
    
    // Send to Slack
    def payload = [
        channel: channel,
        attachments: [[
            color: color,
            text: message,
            footer: 'Jenkins CI'
        ]]
    ]
    
    try {
        def json = groovy.json.JsonOutput.toJson(payload)
        sh(script: "curl -s -X POST -H 'Content-type: application/json' --data '${json}' '${webhookUrl}'", returnStatus: true)
    } catch (Exception e) {
        echo "Failed to send Slack notification: ${e.message}"
    }
}

/**
 * Send simple Slack message.
 */
def slack(String message, String channel = '#builds') {
    call(message: message, channel: channel)
}
