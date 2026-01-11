package jenkins.pipeline.enums

/**
 * Represents deployment environments.
 * Used for environment-specific configuration and deployment targets.
 */
enum Environment implements Serializable {
    
    DEVELOPMENT('dev', 'Development', false, false),
    STAGING('staging', 'Staging', true, false),
    PRODUCTION('prod', 'Production', true, true)

    /** Short identifier used in flavor names */
    final String id
    
    /** Human-readable display name */
    final String displayName
    
    /** Whether this environment requires signing */
    final boolean requiresSigning
    
    /** Whether this is a production environment */
    final boolean isProduction

    private Environment(String id, String displayName, boolean requiresSigning, boolean isProduction) {
        this.id = id
        this.displayName = displayName
        this.requiresSigning = requiresSigning
        this.isProduction = isProduction
    }

    /**
     * Get the product flavor name for this environment.
     * @return Flavor name for Gradle
     */
    String getFlavorName() {
        return id
    }

    /**
     * Get environment-specific configuration file name.
     * @return Config file name pattern
     */
    String getConfigFileName() {
        return ".env.${id}"
    }

    /**
     * Get the full variant name combining environment and build type.
     * @param buildType The build type
     * @return Full variant name (e.g., 'devDebug', 'prodRelease')
     */
    String getVariantName(BuildType buildType) {
        return "${id}${buildType.displayName}"
    }

    /**
     * Parse string to Environment enum.
     * @param value String value
     * @return Environment enum
     */
    static Environment fromString(String value) {
        if (!value) {
            return DEVELOPMENT // Default to dev
        }
        
        String normalizedValue = value.toLowerCase()
        Environment result = values().find { 
            it.id == normalizedValue || it.name().toLowerCase() == normalizedValue 
        }
        
        if (!result) {
            throw new IllegalArgumentException(
                "Invalid environment: '${value}'. Valid values are: ${values()*.id.join(', ')}"
            )
        }
        return result
    }

    /**
     * Check if deployment to Play Store is allowed for this environment.
     */
    boolean isPlayStoreDeploymentAllowed() {
        return this != DEVELOPMENT
    }

    /**
     * Get the recommended Play Store track for this environment.
     */
    PlayStoreTrack getRecommendedTrack() {
        switch (this) {
            case STAGING:
                return PlayStoreTrack.INTERNAL
            case PRODUCTION:
                return PlayStoreTrack.PRODUCTION
            default:
                return null
        }
    }

    @Override
    String toString() {
        return id
    }
}
