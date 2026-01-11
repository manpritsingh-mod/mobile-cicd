package jenkins.pipeline.notification

import jenkins.pipeline.interfaces.INotifier
import jenkins.pipeline.logging.PipelineLogger
import jenkins.pipeline.models.BuildResult
import jenkins.pipeline.models.TestResult

/**
 * Aggregates multiple notification channels.
 * Sends to Slack, Email, and other configured channels.
 */
class NotificationService implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final List<INotifier> notifiers = []

    NotificationService(Object script) {
        this.script = script
        this.logger = PipelineLogger.create(script)
    }

    /**
     * Add a notifier to the service.
     * @param notifier Notifier implementation
     * @return this for chaining
     */
    NotificationService addNotifier(INotifier notifier) {
        if (notifier?.isConfigured()) {
            notifiers << notifier
            logger.debug("Added notifier: ${notifier.channelName}")
        }
        return this
    }

    /**
     * Add Slack notifier.
     * @param webhookUrl Slack webhook URL
     * @param channel Target channel
     * @return this for chaining
     */
    NotificationService withSlack(String webhookUrl, String channel = '#builds') {
        if (webhookUrl) {
            addNotifier(SlackNotifier.create(script, webhookUrl, channel))
        }
        return this
    }

    /**
     * Add Email notifier.
     * @param recipients List of email addresses
     * @return this for chaining
     */
    NotificationService withEmail(List<String> recipients) {
        if (recipients) {
            addNotifier(EmailNotifier.create(script, recipients))
        }
        return this
    }

    /**
     * Notify all channels about build start.
     */
    void notifyBuildStarted(String jobName, int buildNumber, String gitBranch) {
        notifyAll { notifier ->
            notifier.notifyBuildStarted(jobName, buildNumber, gitBranch)
        }
    }

    /**
     * Notify all channels about build success.
     */
    void notifyBuildSuccess(BuildResult buildResult, long duration) {
        notifyAll { notifier ->
            notifier.notifyBuildSuccess(buildResult, duration)
        }
    }

    /**
     * Notify all channels about build failure.
     */
    void notifyBuildFailure(String jobName, int buildNumber, String errorMessage, String stageName) {
        notifyAll { notifier ->
            notifier.notifyBuildFailure(jobName, buildNumber, errorMessage, stageName)
        }
    }

    /**
     * Notify all channels about test results.
     */
    void notifyTestResults(TestResult testResult) {
        notifyAll { notifier ->
            notifier.notifyTestResults(testResult)
        }
    }

    /**
     * Notify all channels about deployment.
     */
    void notifyDeployment(String environment, String version, String track) {
        notifyAll { notifier ->
            notifier.notifyDeployment(environment, version, track)
        }
    }

    /**
     * Send custom message to all channels.
     */
    void sendMessage(String message, String level = 'info') {
        notifyAll { notifier ->
            notifier.sendMessage(message, level)
        }
    }

    /**
     * Check if any notifiers are configured.
     */
    boolean hasNotifiers() {
        return !notifiers.isEmpty()
    }

    /**
     * Get count of configured notifiers.
     */
    int getNotifierCount() {
        return notifiers.size()
    }

    /**
     * Get list of configured channel names.
     */
    List<String> getConfiguredChannels() {
        return notifiers.collect { it.channelName }
    }

    /**
     * Execute notification on all configured notifiers.
     */
    private void notifyAll(Closure action) {
        notifiers.each { notifier ->
            try {
                action(notifier)
            } catch (Exception e) {
                logger.warn("Failed to send notification via ${notifier.channelName}: ${e.message}")
            }
        }
    }

    /**
     * Create from pipeline script.
     */
    static NotificationService create(Object script) {
        return new NotificationService(script)
    }

    /**
     * Create with common configurations from PipelineConfig.
     */
    static NotificationService fromConfig(Object script, 
                                          String slackWebhookUrl, String slackChannel,
                                          List<String> emailRecipients) {
        return create(script)
            .withSlack(slackWebhookUrl, slackChannel)
            .withEmail(emailRecipients)
    }
}
