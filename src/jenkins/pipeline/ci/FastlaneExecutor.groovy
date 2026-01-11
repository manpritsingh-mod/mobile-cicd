package jenkins.pipeline.ci

import jenkins.pipeline.logging.PipelineLogger

/**
 * Simple executor for Fastlane commands.
 */
class FastlaneExecutor implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final String fastlaneDir
    private final String packageName

    FastlaneExecutor(Object script, String fastlaneDir = 'android/fastlane', String packageName = null) {
        this.script = script
        this.logger = PipelineLogger.create(script)
        this.fastlaneDir = fastlaneDir
        this.packageName = packageName ?: script.env.ANDROID_PACKAGE_NAME
    }

    /**
     * Run a Fastlane lane.
     */
    int runLane(String lane, Map options = [:]) {
        String command = buildCommand(lane, options)
        logger.info("Running: ${command}")
        
        return script.dir(fastlaneDir) {
            return script.sh(script: command, returnStatus: true)
        }
    }

    /**
     * Deploy AAB to Play Store.
     */
    boolean deployToPlayStore(String aabPath, String track = 'internal', double rollout = 100.0) {
        logger.section("Deploying to Play Store")
        logger.property("AAB", aabPath)
        logger.property("Track", track)
        
        if (!script.fileExists(aabPath)) {
            logger.error("AAB file not found: ${aabPath}")
            return false
        }
        
        Map options = [
            track: track,
            aab: aabPath,
            rollout: String.valueOf(rollout / 100.0)
        ]
        
        int exitCode = runLane('supply', options)
        
        if (exitCode == 0) {
            logger.success("Deployed successfully to ${track}")
            return true
        } else {
            logger.error("Deployment failed with exit code ${exitCode}")
            return false
        }
    }

    /**
     * Build command string.
     */
    private String buildCommand(String lane, Map options) {
        StringBuilder cmd = new StringBuilder("bundle exec fastlane ${lane}")
        
        options.each { key, value ->
            if (value != null) {
                cmd.append(" ${key}:\"${value}\"")
            }
        }
        
        return cmd.toString()
    }

    static FastlaneExecutor create(Object script) {
        return new FastlaneExecutor(script)
    }
}
