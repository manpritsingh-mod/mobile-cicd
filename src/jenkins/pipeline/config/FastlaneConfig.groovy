package jenkins.pipeline.config

/**
 * Simple Fastlane configuration.
 */
class FastlaneConfig implements Serializable {

    private static final long serialVersionUID = 1L

    String packageName
    String fastlaneDir = 'android/fastlane'
    String jsonKeyPath
    String defaultTrack = 'internal'
    double rolloutPercentage = 100.0

    FastlaneConfig() {}

    FastlaneConfig(Map config) {
        this.packageName = config.packageName
        this.fastlaneDir = config.fastlaneDir ?: 'android/fastlane'
        this.jsonKeyPath = config.jsonKeyPath
        this.defaultTrack = config.defaultTrack ?: 'internal'
        this.rolloutPercentage = config.rolloutPercentage ?: 100.0
    }

    static FastlaneConfig create(Map config = [:]) {
        return new FastlaneConfig(config)
    }
}
