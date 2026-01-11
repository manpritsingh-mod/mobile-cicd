package jenkins.pipeline.interfaces

import jenkins.pipeline.models.BuildResult
import jenkins.pipeline.enums.PlayStoreTrack

/**
 * Interface for distribution targets.
 * Implements Strategy pattern for different distribution channels.
 */
interface IDistributor extends Serializable {

    /**
     * Upload artifact to distribution target.
     * @param artifactPath Path to the artifact (APK/AAB)
     * @param track Distribution track (for Play Store)
     * @param releaseNotes Release notes/changelog
     * @return Map containing upload result (success, url, version)
     */
    Map<String, Object> upload(String artifactPath, PlayStoreTrack track, String releaseNotes)

    /**
     * Promote a release from one track to another.
     * @param fromTrack Source track
     * @param toTrack Destination track
     * @param rolloutPercentage Rollout percentage (0-100)
     * @return true if promotion was successful
     */
    boolean promote(PlayStoreTrack fromTrack, PlayStoreTrack toTrack, double rolloutPercentage)

    /**
     * Get the current release status.
     * @param track Track to check
     * @return Map containing release info (version, status, rollout)
     */
    Map<String, Object> getStatus(PlayStoreTrack track)

    /**
     * Validate distribution credentials and configuration.
     * @return List of validation errors (empty if valid)
     */
    List<String> validateConfig()

    /**
     * Get the distribution target name.
     * @return Human-readable target name
     */
    String getTargetName()

    /**
     * Rollback to a previous version.
     * @param track Track to rollback
     * @param versionCode Version code to rollback to
     * @return true if rollback was successful
     */
    boolean rollback(PlayStoreTrack track, int versionCode)
}
