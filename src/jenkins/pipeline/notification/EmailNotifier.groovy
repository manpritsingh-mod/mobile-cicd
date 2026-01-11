package jenkins.pipeline.notification

import jenkins.pipeline.interfaces.INotifier
import jenkins.pipeline.logging.PipelineLogger
import jenkins.pipeline.models.BuildResult
import jenkins.pipeline.models.TestResult

/**
 * Email notification implementation.
 * Sends HTML formatted emails via Jenkins email plugin.
 */
class EmailNotifier implements INotifier, Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final List<String> recipients
    private final String fromAddress
    private final String replyTo

    EmailNotifier(Object script, List<String> recipients, 
                  String fromAddress = 'jenkins@company.com',
                  String replyTo = null) {
        this.script = script
        this.logger = PipelineLogger.create(script)
        this.recipients = recipients ?: []
        this.fromAddress = fromAddress
        this.replyTo = replyTo ?: fromAddress
    }

    @Override
    void notifyBuildStarted(String jobName, int buildNumber, String gitBranch) {
        String subject = "🚀 Build Started: ${jobName} #${buildNumber}"
        String body = buildHtmlEmail([
            title: "Build Started",
            status: 'info',
            sections: [
                [label: 'Job', value: jobName],
                [label: 'Build Number', value: "#${buildNumber}"],
                [label: 'Branch', value: gitBranch]
            ]
        ])
        
        sendEmail(subject, body)
    }

    @Override
    void notifyBuildSuccess(BuildResult buildResult, long duration) {
        String subject = "✅ Build Successful: ${buildResult.variantName}"
        String body = buildHtmlEmail([
            title: "Build Successful",
            status: 'success',
            sections: [
                [label: 'Version', value: buildResult.version?.versionName ?: 'N/A'],
                [label: 'Variant', value: buildResult.variantName],
                [label: 'Duration', value: buildResult.formattedDuration],
                [label: 'Artifact Size', value: buildResult.formattedFileSize],
                [label: 'Artifact', value: buildResult.artifactFileName]
            ]
        ])
        
        sendEmail(subject, body)
    }

    @Override
    void notifyBuildFailure(String jobName, int buildNumber, String errorMessage, String stageName) {
        String subject = "❌ Build Failed: ${jobName} #${buildNumber}"
        String body = buildHtmlEmail([
            title: "Build Failed",
            status: 'failure',
            sections: [
                [label: 'Job', value: jobName],
                [label: 'Build Number', value: "#${buildNumber}"],
                [label: 'Failed Stage', value: stageName ?: 'Unknown'],
                [label: 'Error', value: errorMessage?.take(500) ?: 'Unknown error']
            ],
            footer: "Check Jenkins console output for full details."
        ])
        
        sendEmail(subject, body)
    }

    @Override
    void notifyTestResults(TestResult testResult) {
        String status = testResult.success ? 'success' : 'failure'
        String icon = testResult.success ? '✅' : '❌'
        String subject = "${icon} Test Results: ${testResult.framework}"
        
        List<Map> sections = [
            [label: 'Framework', value: testResult.framework],
            [label: 'Total Tests', value: testResult.totalTests.toString()],
            [label: 'Passed', value: testResult.passedTests.toString()],
            [label: 'Failed', value: testResult.failedTests.toString()],
            [label: 'Skipped', value: testResult.skippedTests.toString()],
            [label: 'Duration', value: testResult.formattedDuration]
        ]
        
        if (testResult.coveragePercent != null) {
            sections << [label: 'Coverage', value: "${String.format('%.1f', testResult.coveragePercent)}%"]
        }
        
        String body = buildHtmlEmail([
            title: "Test Results: ${testResult.framework}",
            status: status,
            sections: sections
        ])
        
        sendEmail(subject, body)
    }

    @Override
    void notifyDeployment(String environment, String version, String track) {
        String subject = "🚀 Deployment Complete: ${version} to ${environment}"
        String body = buildHtmlEmail([
            title: "Deployment Complete",
            status: 'success',
            sections: [
                [label: 'Environment', value: environment],
                [label: 'Version', value: version],
                [label: 'Track', value: track]
            ]
        ])
        
        sendEmail(subject, body)
    }

    @Override
    void sendMessage(String message, String level) {
        String subject = "${getIconForLevel(level)} Jenkins Notification"
        String body = buildHtmlEmail([
            title: "Notification",
            status: level,
            message: message
        ])
        
        sendEmail(subject, body)
    }

    @Override
    boolean isConfigured() {
        return recipients && !recipients.isEmpty()
    }

    @Override
    String getChannelName() {
        return "Email"
    }

    /**
     * Send email via Jenkins.
     */
    private void sendEmail(String subject, String body) {
        if (!isConfigured()) {
            logger.warn("Email recipients not configured, skipping notification")
            return
        }
        
        try {
            script.emailext(
                subject: subject,
                body: body,
                to: recipients.join(','),
                from: fromAddress,
                replyTo: replyTo,
                mimeType: 'text/html'
            )
            logger.info("Email sent to ${recipients.size()} recipients")
        } catch (Exception e) {
            logger.warn("Failed to send email: ${e.message}")
        }
    }

    /**
     * Build HTML email body.
     */
    private String buildHtmlEmail(Map<String, Object> data) {
        String statusColor = getColorForStatus(data.status?.toString())
        
        StringBuilder html = new StringBuilder()
        html.append("""
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: ${statusColor}; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
        .content { background-color: #f5f5f5; padding: 20px; border-radius: 0 0 8px 8px; }
        .section { margin: 10px 0; padding: 10px; background-color: white; border-radius: 4px; }
        .label { font-weight: bold; color: #666; }
        .value { color: #333; }
        .footer { margin-top: 20px; font-size: 12px; color: #999; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>${data.title ?: 'Jenkins Notification'}</h2>
        </div>
        <div class="content">
""")
        
        // Add message if present
        if (data.message) {
            html.append("<p>${data.message}</p>")
        }
        
        // Add sections
        data.sections?.each { section ->
            html.append("""
            <div class="section">
                <span class="label">${section.label}:</span>
                <span class="value">${section.value}</span>
            </div>
""")
        }
        
        html.append("""
            <div class="footer">
                ${data.footer ?: 'Sent by Jenkins CI'}
            </div>
        </div>
    </div>
</body>
</html>
""")
        
        return html.toString()
    }

    private String getColorForStatus(String status) {
        switch (status?.toLowerCase()) {
            case 'success': return '#28a745'
            case 'failure': return '#dc3545'
            case 'warning': return '#ffc107'
            default: return '#007bff'
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
    static EmailNotifier create(Object script, List<String> recipients) {
        return new EmailNotifier(script, recipients)
    }
}
