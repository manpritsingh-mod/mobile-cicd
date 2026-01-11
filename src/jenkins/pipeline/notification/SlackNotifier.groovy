package jenkins.pipeline.notification

import jenkins.pipeline.interfaces.INotifier
import jenkins.pipeline.logging.PipelineLogger
import jenkins.pipeline.models.BuildResult
import jenkins.pipeline.models.TestResult
import groovy.json.JsonOutput

/**
 * Slack notification implementation.
 * Sends rich messages to Slack via webhooks.
 */
class SlackNotifier implements INotifier, Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final String webhookUrl
    private final String channel
    private final String username
    private final String iconEmoji

    SlackNotifier(Object script, String webhookUrl, String channel = '#builds',
                  String username = 'Jenkins CI', String iconEmoji = ':jenkins:') {
        this.script = script
        this.logger = PipelineLogger.create(script)
        this.webhookUrl = webhookUrl
        this.channel = channel
        this.username = username
        this.iconEmoji = iconEmoji
    }

    @Override
    void notifyBuildStarted(String jobName, int buildNumber, String gitBranch) {
        Map<String, Object> attachment = [
            color: '#439FE0',
            title: "🚀 Build Started: ${jobName} #${buildNumber}",
            fields: [
                [title: 'Branch', value: gitBranch, short: true],
                [title: 'Build', value: "#${buildNumber}", short: true]
            ],
            footer: 'Jenkins CI',
            ts: System.currentTimeMillis() / 1000
        ]
        
        sendSlackMessage([attachments: [attachment]])
    }

    @Override
    void notifyBuildSuccess(BuildResult buildResult, long duration) {
        String durationStr = formatDuration(duration)
        
        Map<String, Object> attachment = [
            color: 'good',
            title: "✅ Build Successful",
            fields: [
                [title: 'Version', value: buildResult.version?.versionName ?: 'N/A', short: true],
                [title: 'Duration', value: durationStr, short: true],
                [title: 'Variant', value: buildResult.variantName ?: 'N/A', short: true],
                [title: 'Size', value: buildResult.formattedFileSize, short: true]
            ],
            footer: 'Jenkins CI',
            ts: System.currentTimeMillis() / 1000
        ]
        
        if (buildResult.artifactPath) {
            attachment['text'] = "Artifact: `${buildResult.artifactFileName}`"
        }
        
        sendSlackMessage([attachments: [attachment]])
    }

    @Override
    void notifyBuildFailure(String jobName, int buildNumber, String errorMessage, String stageName) {
        Map<String, Object> attachment = [
            color: 'danger',
            title: "❌ Build Failed: ${jobName} #${buildNumber}",
            text: errorMessage?.take(500),
            fields: [
                [title: 'Failed Stage', value: stageName ?: 'Unknown', short: true],
                [title: 'Build', value: "#${buildNumber}", short: true]
            ],
            footer: 'Jenkins CI',
            ts: System.currentTimeMillis() / 1000
        ]
        
        sendSlackMessage([attachments: [attachment]])
    }

    @Override
    void notifyTestResults(TestResult testResult) {
        String color = testResult.success ? 'good' : 'danger'
        String icon = testResult.success ? '✅' : '❌'
        
        Map<String, Object> attachment = [
            color: color,
            title: "${icon} Test Results: ${testResult.framework}",
            fields: [
                [title: 'Total', value: testResult.totalTests.toString(), short: true],
                [title: 'Passed', value: testResult.passedTests.toString(), short: true],
                [title: 'Failed', value: testResult.failedTests.toString(), short: true],
                [title: 'Duration', value: testResult.formattedDuration, short: true]
            ],
            footer: 'Jenkins CI',
            ts: System.currentTimeMillis() / 1000
        ]
        
        // Add coverage if available
        if (testResult.coveragePercent != null) {
            attachment['fields'] << [
                title: 'Coverage',
                value: "${String.format('%.1f', testResult.coveragePercent)}%",
                short: true
            ]
        }
        
        // Add failed test names if any
        if (testResult.failedTestNames) {
            String failedTests = testResult.failedTestNames.take(5).join('\n• ')
            if (testResult.failedTestNames.size() > 5) {
                failedTests += "\n... and ${testResult.failedTestNames.size() - 5} more"
            }
            attachment['text'] = "Failed tests:\n• ${failedTests}"
        }
        
        sendSlackMessage([attachments: [attachment]])
    }

    @Override
    void notifyDeployment(String environment, String version, String track) {
        Map<String, Object> attachment = [
            color: '#9B59B6',
            title: "🚀 Deployment Complete",
            fields: [
                [title: 'Environment', value: environment, short: true],
                [title: 'Version', value: version, short: true],
                [title: 'Track', value: track, short: true]
            ],
            footer: 'Jenkins CI',
            ts: System.currentTimeMillis() / 1000
        ]
        
        sendSlackMessage([attachments: [attachment]])
    }

    @Override
    void sendMessage(String message, String level) {
        String color = getColorForLevel(level)
        String icon = getIconForLevel(level)
        
        Map<String, Object> attachment = [
            color: color,
            text: "${icon} ${message}",
            footer: 'Jenkins CI',
            ts: System.currentTimeMillis() / 1000
        ]
        
        sendSlackMessage([attachments: [attachment]])
    }

    @Override
    boolean isConfigured() {
        return webhookUrl?.trim()
    }

    @Override
    String getChannelName() {
        return "Slack"
    }

    /**
     * Send message to Slack webhook.
     */
    private void sendSlackMessage(Map<String, Object> payload) {
        if (!isConfigured()) {
            logger.warn("Slack webhook URL not configured, skipping notification")
            return
        }
        
        payload['channel'] = channel
        payload['username'] = username
        payload['icon_emoji'] = iconEmoji
        
        String jsonPayload = JsonOutput.toJson(payload)
        
        try {
            script.sh(
                script: """
                    curl -s -X POST -H 'Content-type: application/json' \\
                    --data '${jsonPayload.replace("'", "'\\''")}' \\
                    '${webhookUrl}'
                """,
                returnStatus: true
            )
        } catch (Exception e) {
            logger.warn("Failed to send Slack notification: ${e.message}")
        }
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000
        long minutes = seconds / 60
        seconds = seconds % 60
        return minutes > 0 ? "${minutes}m ${seconds}s" : "${seconds}s"
    }

    private String getColorForLevel(String level) {
        switch (level?.toLowerCase()) {
            case 'error': return 'danger'
            case 'warning': return 'warning'
            case 'success': return 'good'
            default: return '#439FE0'
        }
    }

    private String getIconForLevel(String level) {
        switch (level?.toLowerCase()) {
            case 'error': return '❌'
            case 'warning': return '⚠️'
            case 'success': return '✅'
            default: return 'ℹ️'
        }
    }

    /**
     * Create from pipeline script.
     */
    static SlackNotifier create(Object script, String webhookUrl, String channel = '#builds') {
        return new SlackNotifier(script, webhookUrl, channel)
    }
}
