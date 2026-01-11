package jenkins.pipeline.interfaces

import jenkins.pipeline.models.BuildResult
import jenkins.pipeline.models.TestResult

/**
 * Interface for notification services.
 * Supports multiple notification channels (Slack, Email, etc.)
 */
interface INotifier extends Serializable {

    /**
     * Send build started notification.
     * @param jobName Jenkins job name
     * @param buildNumber Build number
     * @param gitBranch Git branch being built
     */
    void notifyBuildStarted(String jobName, int buildNumber, String gitBranch)

    /**
     * Send build success notification.
     * @param buildResult Build result with artifact info
     * @param duration Build duration in milliseconds
     */
    void notifyBuildSuccess(BuildResult buildResult, long duration)

    /**
     * Send build failure notification.
     * @param jobName Jenkins job name
     * @param buildNumber Build number
     * @param errorMessage Error message
     * @param stageName Failed stage name
     */
    void notifyBuildFailure(String jobName, int buildNumber, String errorMessage, String stageName)

    /**
     * Send test results notification.
     * @param testResult Test execution results
     */
    void notifyTestResults(TestResult testResult)

    /**
     * Send deployment notification.
     * @param environment Target environment
     * @param version Deployed version
     * @param track Play Store track (if applicable)
     */
    void notifyDeployment(String environment, String version, String track)

    /**
     * Send a custom message.
     * @param message Message content
     * @param level Severity level (info, warning, error)
     */
    void sendMessage(String message, String level)

    /**
     * Check if the notifier is properly configured.
     * @return true if configuration is valid
     */
    boolean isConfigured()

    /**
     * Get the notification channel name.
     * @return Channel name (e.g., "Slack", "Email")
     */
    String getChannelName()
}
