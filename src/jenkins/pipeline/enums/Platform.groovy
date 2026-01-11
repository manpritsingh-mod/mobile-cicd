package jenkins.pipeline.enums

/**
 * Represents the target platform for mobile builds.
 * Currently supports Android, extensible for iOS.
 */
enum Platform implements Serializable {
    
    ANDROID('android', 'Android', 'gradle'),
    IOS('ios', 'iOS', 'xcodebuild')

    /** Platform identifier */
    final String id
    
    /** Display name */
    final String displayName
    
    /** Primary build tool */
    final String buildTool

    private Platform(String id, String displayName, String buildTool) {
        this.id = id
        this.displayName = displayName
        this.buildTool = buildTool
    }

    /**
     * Get the project subdirectory for this platform.
     * @return Relative path to platform directory
     */
    String getProjectDir() {
        return id
    }

    /**
     * Get the Fastlane platform identifier.
     * @return Fastlane platform string
     */
    String getFastlanePlatform() {
        return id
    }

    /**
     * Parse string to Platform enum.
     * @param value String value
     * @return Platform enum
     */
    static Platform fromString(String value) {
        if (!value) {
            throw new IllegalArgumentException("Platform cannot be null or empty")
        }
        try {
            return valueOf(value.toUpperCase())
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid platform: '${value}'. Valid values are: ${values()*.name().join(', ')}"
            )
        }
    }

    /**
     * Check if this platform is currently supported.
     */
    boolean isSupported() {
        return this == ANDROID // iOS support can be added later
    }

    @Override
    String toString() {
        return id
    }
}
