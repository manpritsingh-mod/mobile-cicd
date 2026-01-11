package jenkins.pipeline.config

import groovy.transform.ToString
import jenkins.pipeline.enums.BuildType
import jenkins.pipeline.enums.Environment
import jenkins.pipeline.enums.OutputType
import jenkins.pipeline.enums.PlayStoreTrack

/**
 * Central pipeline configuration.
 * Contains all settings needed for CI/CD execution.
 */
@ToString(includeNames = true, includePackage = false)
class PipelineConfig implements Serializable {

    private static final long serialVersionUID = 1L

    // === Git Configuration ===
    /** Git repository URL */
    final String gitUrl
    
    /** Git branch or ref to build */
    final String gitBranch
    
    /** Credentials ID for Git access */
    final String gitCredentialsId

    // === Docker Configuration ===
    /** Nexus Docker registry URL */
    final String dockerRegistry
    
    /** Docker image to use for build */
    final String dockerImage
    
    /** Docker image tag */
    final String dockerTag

    // === Build Configuration ===
    /** Application name */
    final String appName
    
    /** Build type */
    final BuildType buildType
    
    /** Target environment */
    final Environment environment
    
    /** Output type (APK/AAB) */
    final OutputType outputType
    
    /** Node.js version */
    final String nodeVersion
    
    /** Java version */
    final String javaVersion

    // === Signing Configuration ===
    /** Keystore credentials ID */
    final String keystoreCredentialsId
    
    /** Key alias */
    final String keyAlias
    
    /** Keystore password credentials ID */
    final String keystorePasswordCredentialsId

    // === Deployment Configuration ===
    /** Play Store JSON key credentials ID */
    final String playStoreCredentialsId
    
    /** Target Play Store track */
    final PlayStoreTrack playStoreTrack
    
    /** Rollout percentage */
    final double rolloutPercentage

    // === Notification Configuration ===
    /** Slack webhook URL credentials ID */
    final String slackWebhookCredentialsId
    
    /** Slack channel */
    final String slackChannel
    
    /** Email recipients */
    final List<String> emailRecipients

    // === Testing Configuration ===
    /** Run unit tests */
    final boolean runUnitTests
    
    /** Run E2E tests */
    final boolean runE2ETests
    
    /** Docker emulator URL for Appium */
    final String emulatorUrl

    // === Timeouts ===
    /** Build timeout in minutes */
    final int buildTimeout
    
    /** Test timeout in minutes */
    final int testTimeout

    private PipelineConfig(Builder builder) {
        // Git
        this.gitUrl = builder.gitUrl
        this.gitBranch = builder.gitBranch ?: 'main'
        this.gitCredentialsId = builder.gitCredentialsId
        
        // Docker
        this.dockerRegistry = builder.dockerRegistry ?: '43.88.89.25:5000'
        this.dockerImage = builder.dockerImage ?: 'react-native-android'
        this.dockerTag = builder.dockerTag ?: 'latest'
        
        // Build
        this.appName = builder.appName
        this.buildType = builder.buildType ?: BuildType.DEBUG
        this.environment = builder.environment ?: Environment.DEVELOPMENT
        this.outputType = builder.outputType ?: OutputType.APK
        this.nodeVersion = builder.nodeVersion ?: '20'
        this.javaVersion = builder.javaVersion ?: '17'
        
        // Signing
        this.keystoreCredentialsId = builder.keystoreCredentialsId
        this.keyAlias = builder.keyAlias
        this.keystorePasswordCredentialsId = builder.keystorePasswordCredentialsId
        
        // Deployment
        this.playStoreCredentialsId = builder.playStoreCredentialsId
        this.playStoreTrack = builder.playStoreTrack ?: PlayStoreTrack.INTERNAL
        this.rolloutPercentage = builder.rolloutPercentage
        
        // Notifications
        this.slackWebhookCredentialsId = builder.slackWebhookCredentialsId
        this.slackChannel = builder.slackChannel ?: '#builds'
        this.emailRecipients = builder.emailRecipients?.asImmutable() ?: []
        
        // Testing
        this.runUnitTests = builder.runUnitTests
        this.runE2ETests = builder.runE2ETests
        this.emulatorUrl = builder.emulatorUrl
        
        // Timeouts
        this.buildTimeout = builder.buildTimeout > 0 ? builder.buildTimeout : 30
        this.testTimeout = builder.testTimeout > 0 ? builder.testTimeout : 20
    }

    /**
     * Get full Docker image path.
     */
    String getFullDockerImage() {
        return "${dockerRegistry}/${dockerImage}:${dockerTag}"
    }

    /**
     * Check if this is a release build.
     */
    boolean isReleaseBuild() {
        return buildType == BuildType.RELEASE
    }

    /**
     * Check if deployment is enabled.
     */
    boolean isDeploymentEnabled() {
        return playStoreCredentialsId && environment.isPlayStoreDeploymentAllowed()
    }

    /**
     * Check if notifications are enabled.
     */
    boolean isNotificationsEnabled() {
        return slackWebhookCredentialsId || emailRecipients
    }

    /**
     * Get variant name for this configuration.
     */
    String getVariantName() {
        return environment.getVariantName(buildType)
    }

    /**
     * Create builder from existing config.
     */
    Builder toBuilder() {
        return new Builder()
            .gitUrl(gitUrl)
            .gitBranch(gitBranch)
            .gitCredentialsId(gitCredentialsId)
            .dockerRegistry(dockerRegistry)
            .dockerImage(dockerImage)
            .dockerTag(dockerTag)
            .appName(appName)
            .buildType(buildType)
            .environment(environment)
            .outputType(outputType)
            .nodeVersion(nodeVersion)
            .javaVersion(javaVersion)
            .keystoreCredentialsId(keystoreCredentialsId)
            .keyAlias(keyAlias)
            .keystorePasswordCredentialsId(keystorePasswordCredentialsId)
            .playStoreCredentialsId(playStoreCredentialsId)
            .playStoreTrack(playStoreTrack)
            .rolloutPercentage(rolloutPercentage)
            .slackWebhookCredentialsId(slackWebhookCredentialsId)
            .slackChannel(slackChannel)
            .emailRecipients(new ArrayList<>(emailRecipients))
            .runUnitTests(runUnitTests)
            .runE2ETests(runE2ETests)
            .emulatorUrl(emulatorUrl)
            .buildTimeout(buildTimeout)
            .testTimeout(testTimeout)
    }

    static Builder builder() {
        return new Builder()
    }

    /**
     * Builder for PipelineConfig.
     */
    static class Builder implements Serializable {
        private static final long serialVersionUID = 1L

        String gitUrl
        String gitBranch = 'main'
        String gitCredentialsId
        String dockerRegistry = '43.88.89.25:5000'
        String dockerImage = 'react-native-android'
        String dockerTag = 'latest'
        String appName
        BuildType buildType = BuildType.DEBUG
        Environment environment = Environment.DEVELOPMENT
        OutputType outputType = OutputType.APK
        String nodeVersion = '20'
        String javaVersion = '17'
        String keystoreCredentialsId
        String keyAlias
        String keystorePasswordCredentialsId
        String playStoreCredentialsId
        PlayStoreTrack playStoreTrack = PlayStoreTrack.INTERNAL
        double rolloutPercentage = 100.0
        String slackWebhookCredentialsId
        String slackChannel = '#builds'
        List<String> emailRecipients = []
        boolean runUnitTests = true
        boolean runE2ETests = false
        String emulatorUrl
        int buildTimeout = 30
        int testTimeout = 20

        Builder gitUrl(String url) { this.gitUrl = url; return this }
        Builder gitBranch(String branch) { this.gitBranch = branch; return this }
        Builder gitCredentialsId(String id) { this.gitCredentialsId = id; return this }
        Builder dockerRegistry(String registry) { this.dockerRegistry = registry; return this }
        Builder dockerImage(String image) { this.dockerImage = image; return this }
        Builder dockerTag(String tag) { this.dockerTag = tag; return this }
        Builder appName(String name) { this.appName = name; return this }
        Builder buildType(BuildType type) { this.buildType = type; return this }
        Builder buildType(String type) { this.buildType = BuildType.fromString(type); return this }
        Builder environment(Environment env) { this.environment = env; return this }
        Builder environment(String env) { this.environment = Environment.fromString(env); return this }
        Builder outputType(OutputType type) { this.outputType = type; return this }
        Builder outputType(String type) { this.outputType = OutputType.fromString(type); return this }
        Builder nodeVersion(String version) { this.nodeVersion = version; return this }
        Builder javaVersion(String version) { this.javaVersion = version; return this }
        Builder keystoreCredentialsId(String id) { this.keystoreCredentialsId = id; return this }
        Builder keyAlias(String alias) { this.keyAlias = alias; return this }
        Builder keystorePasswordCredentialsId(String id) { this.keystorePasswordCredentialsId = id; return this }
        Builder playStoreCredentialsId(String id) { this.playStoreCredentialsId = id; return this }
        Builder playStoreTrack(PlayStoreTrack track) { this.playStoreTrack = track; return this }
        Builder playStoreTrack(String track) { this.playStoreTrack = PlayStoreTrack.fromString(track); return this }
        Builder rolloutPercentage(double pct) { this.rolloutPercentage = pct; return this }
        Builder slackWebhookCredentialsId(String id) { this.slackWebhookCredentialsId = id; return this }
        Builder slackChannel(String channel) { this.slackChannel = channel; return this }
        Builder emailRecipients(List<String> recipients) { this.emailRecipients = recipients ?: []; return this }
        Builder addEmailRecipient(String email) { this.emailRecipients << email; return this }
        Builder runUnitTests(boolean run) { this.runUnitTests = run; return this }
        Builder runE2ETests(boolean run) { this.runE2ETests = run; return this }
        Builder emulatorUrl(String url) { this.emulatorUrl = url; return this }
        Builder buildTimeout(int minutes) { this.buildTimeout = minutes; return this }
        Builder testTimeout(int minutes) { this.testTimeout = minutes; return this }

        PipelineConfig build() {
            validate()
            return new PipelineConfig(this)
        }

        private void validate() {
            List<String> errors = []
            if (!appName?.trim()) errors << "appName is required"
            if (!gitUrl?.trim() && !gitCredentialsId) {
                // Git info might be provided via Jenkins job config
            }
            if (errors) {
                throw new IllegalStateException("PipelineConfig validation failed: ${errors.join(', ')}")
            }
        }
    }
}
