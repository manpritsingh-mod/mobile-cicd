package jenkins.pipeline.enums

/**
 * Represents Google Play Store release tracks.
 * Used for deployment configuration and Fastlane integration.
 */
enum PlayStoreTrack implements Serializable {
    
    INTERNAL('internal', 'Internal Testing', 100, true),
    ALPHA('alpha', 'Closed Testing (Alpha)', 1000, true),
    BETA('beta', 'Open Testing (Beta)', 10000, true),
    PRODUCTION('production', 'Production', -1, false)

    /** Track identifier for Play Store API and Fastlane */
    final String trackName
    
    /** Human-readable description */
    final String description
    
    /** Maximum testers allowed (-1 for unlimited) */
    final int maxTesters
    
    /** Whether this is a testing track */
    final boolean isTestingTrack

    private PlayStoreTrack(String trackName, String description, int maxTesters, boolean isTestingTrack) {
        this.trackName = trackName
        this.description = description
        this.maxTesters = maxTesters
        this.isTestingTrack = isTestingTrack
    }

    /**
     * Get the Fastlane track parameter value.
     * @return Track name for Fastlane supply action
     */
    String getFastlaneTrack() {
        return trackName
    }

    /**
     * Check if rollout percentage is applicable for this track.
     * Only production track supports staged rollouts.
     */
    boolean supportsRolloutPercentage() {
        return this == PRODUCTION
    }

    /**
     * Get the next track in the promotion path.
     * @return Next track or null if already at production
     */
    PlayStoreTrack getNextTrack() {
        switch (this) {
            case INTERNAL:
                return ALPHA
            case ALPHA:
                return BETA
            case BETA:
                return PRODUCTION
            default:
                return null
        }
    }

    /**
     * Get the previous track for rollback.
     * @return Previous track or null if at internal
     */
    PlayStoreTrack getPreviousTrack() {
        switch (this) {
            case PRODUCTION:
                return BETA
            case BETA:
                return ALPHA
            case ALPHA:
                return INTERNAL
            default:
                return null
        }
    }

    /**
     * Parse string to PlayStoreTrack enum.
     * @param value String value
     * @return PlayStoreTrack enum
     */
    static PlayStoreTrack fromString(String value) {
        if (!value) {
            return INTERNAL // Default to internal testing
        }
        
        String normalizedValue = value.toLowerCase()
        PlayStoreTrack result = values().find { 
            it.trackName == normalizedValue || it.name().toLowerCase() == normalizedValue 
        }
        
        if (!result) {
            throw new IllegalArgumentException(
                "Invalid Play Store track: '${value}'. Valid values are: ${values()*.trackName.join(', ')}"
            )
        }
        return result
    }

    /**
     * Validate rollout percentage for this track.
     * @param percentage Rollout percentage (0-100)
     * @throws IllegalArgumentException if percentage is invalid for this track
     */
    void validateRolloutPercentage(double percentage) {
        if (!supportsRolloutPercentage() && percentage < 100) {
            throw new IllegalArgumentException(
                "Staged rollout is only supported for ${PRODUCTION.trackName} track"
            )
        }
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                "Rollout percentage must be between 0 and 100, got: ${percentage}"
            )
        }
    }

    @Override
    String toString() {
        return trackName
    }
}
