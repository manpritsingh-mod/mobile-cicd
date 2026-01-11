package jenkins.pipeline.enums

/**
 * Represents the build type for Android builds.
 * Maps to Gradle build variants and determines signing configuration.
 */
enum BuildType implements Serializable {
    
    DEBUG('debug', 'Debug', false),
    RELEASE('release', 'Release', true),
    STAGING('staging', 'Staging', true)

    /** Gradle build type identifier */
    final String gradleName
    
    /** Human-readable display name */
    final String displayName
    
    /** Whether this build type requires signing */
    final boolean requiresSigning

    private BuildType(String gradleName, String displayName, boolean requiresSigning) {
        this.gradleName = gradleName
        this.displayName = displayName
        this.requiresSigning = requiresSigning
    }

    /**
     * Get the Gradle assemble task for this build type.
     * @param flavor Optional product flavor (e.g., 'dev', 'prod')
     * @return Gradle task name (e.g., 'assembleDevDebug')
     */
    String getAssembleTask(String flavor = null) {
        String flavorPart = flavor ? flavor.capitalize() : ''
        return "assemble${flavorPart}${displayName}"
    }

    /**
     * Get the Gradle bundle task for this build type (AAB).
     * @param flavor Optional product flavor
     * @return Gradle task name (e.g., 'bundleProdRelease')
     */
    String getBundleTask(String flavor = null) {
        String flavorPart = flavor ? flavor.capitalize() : ''
        return "bundle${flavorPart}${displayName}"
    }

    /**
     * Get the Gradle test task for this build type.
     * @param flavor Optional product flavor
     * @return Gradle task name
     */
    String getTestTask(String flavor = null) {
        String flavorPart = flavor ? flavor.capitalize() : ''
        return "test${flavorPart}${displayName}UnitTest"
    }

    /**
     * Parse a string to BuildType enum.
     * @param value String representation
     * @return BuildType enum value
     * @throws IllegalArgumentException if value is invalid
     */
    static BuildType fromString(String value) {
        if (!value) {
            throw new IllegalArgumentException("Build type cannot be null or empty")
        }
        try {
            return valueOf(value.toUpperCase())
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid build type: '${value}'. Valid values are: ${values()*.name().join(', ')}"
            )
        }
    }

    /**
     * Check if this is a debuggable build.
     */
    boolean isDebuggable() {
        return this == DEBUG
    }

    @Override
    String toString() {
        return gradleName
    }
}
