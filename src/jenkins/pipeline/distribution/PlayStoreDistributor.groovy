package jenkins.pipeline.distribution

import jenkins.pipeline.interfaces.IDistributor
import jenkins.pipeline.ci.FastlaneExecutor
import jenkins.pipeline.config.FastlaneConfig
import jenkins.pipeline.enums.PlayStoreTrack
import jenkins.pipeline.exceptions.DeployException
import jenkins.pipeline.logging.PipelineLogger

/**
 * Google Play Store distributor implementation.
 * Uses Fastlane for Play Store deployment.
 */
class PlayStoreDistributor implements IDistributor, Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final FastlaneExecutor fastlane
    private final String packageName

    PlayStoreDistributor(Object script, FastlaneConfig config) {
        this.script = script
        this.logger = PipelineLogger.create(script)
        this.fastlane = FastlaneExecutor.create(script, config)
        this.packageName = config.packageName
    }

    @Override
    Map<String, Object> upload(String artifactPath, PlayStoreTrack track, String releaseNotes) {
        logger.section("Uploading to Play Store")
        logger.property("Artifact", artifactPath)
        logger.property("Track", track.description)
        
        // Validate artifact exists
        if (!script.fileExists(artifactPath)) {
            throw new DeployException(
                "Artifact not found: ${artifactPath}",
                track,
                artifactPath
            )
        }
        
        // Deploy via Fastlane
        Map<String, Object> result = fastlane.deployToPlayStore(
            artifactPath, 
            track, 
            releaseNotes
        )
        
        logger.success("Upload completed successfully")
        
        return result
    }

    @Override
    boolean promote(PlayStoreTrack fromTrack, PlayStoreTrack toTrack, double rolloutPercentage) {
        logger.section("Promoting Release")
        logger.property("From", fromTrack.description)
        logger.property("To", toTrack.description)
        logger.property("Rollout", "${rolloutPercentage}%")
        
        try {
            fastlane.promoteRelease(fromTrack, toTrack, rolloutPercentage)
            logger.success("Promotion completed")
            return true
        } catch (Exception e) {
            logger.error("Promotion failed: ${e.message}")
            return false
        }
    }

    @Override
    Map<String, Object> getStatus(PlayStoreTrack track) {
        logger.info("Getting status for track: ${track.trackName}")
        
        try {
            return fastlane.getPlayStoreVersion(track)
        } catch (Exception e) {
            logger.warn("Failed to get track status: ${e.message}")
            return [error: e.message]
        }
    }

    @Override
    List<String> validateConfig() {
        return fastlane.validate()
    }

    @Override
    String getTargetName() {
        return "Google Play Store"
    }

    @Override
    boolean rollback(PlayStoreTrack track, int versionCode) {
        logger.section("Rolling Back Release")
        logger.property("Track", track.trackName)
        logger.property("Version Code", versionCode)
        
        // Note: Google Play doesn't support true rollback
        // This would require re-promoting a previous version
        logger.warn("Play Store rollback requires manual intervention")
        
        return false
    }

    /**
     * Create from pipeline script and config.
     */
    static PlayStoreDistributor create(Object script, FastlaneConfig config) {
        return new PlayStoreDistributor(script, config)
    }
}
