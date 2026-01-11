package jenkins.pipeline.mobile

import jenkins.pipeline.models.BuildConfig
import jenkins.pipeline.interfaces.IBuilder
import jenkins.pipeline.logging.PipelineLogger

/**
 * Simple Android builder for React Native apps.
 */
class AndroidBuilder implements IBuilder, Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger

    AndroidBuilder(Object script) {
        this.script = script
        this.logger = PipelineLogger.create(script)
    }

    @Override
    Map build(BuildConfig config) {
        logger.stage("Android Build")
        long startTime = System.currentTimeMillis()
        
        try {
            // Clean if needed
            if (config.cleanBuild) {
                clean()
            }
            
            // Get Gradle task
            String task = config.gradleTask
            logger.info("Running: ./gradlew ${task}")
            
            // Execute build
            int exitCode = script.dir('android') {
                return script.sh(
                    script: "./gradlew ${task} -PversionName=${config.version} -PversionCode=${config.versionCode}",
                    returnStatus: true
                )
            }
            
            if (exitCode != 0) {
                return [success: false, error: "Build failed with exit code ${exitCode}"]
            }
            
            // Find artifact
            String artifactPath = findArtifact(config)
            long duration = System.currentTimeMillis() - startTime
            
            logger.success("Build completed in ${duration / 1000}s")
            
            return [
                success: true,
                artifactPath: artifactPath,
                version: config.version,
                versionCode: config.versionCode,
                duration: duration
            ]
            
        } catch (Exception e) {
            logger.error("Build failed: ${e.message}")
            return [success: false, error: e.message]
        }
    }

    @Override
    boolean clean() {
        logger.info("Cleaning build...")
        int exitCode = script.dir('android') {
            return script.sh(script: './gradlew clean', returnStatus: true)
        }
        return exitCode == 0
    }

    @Override
    boolean installDependencies() {
        logger.info("Installing dependencies...")
        
        String cmd = script.fileExists('yarn.lock') ? 
            'yarn install --frozen-lockfile' : 'npm ci'
        
        int exitCode = script.sh(script: cmd, returnStatus: true)
        return exitCode == 0
    }

    @Override
    boolean runLint() {
        logger.info("Running lint...")
        int exitCode = script.sh(script: 'npm run lint', returnStatus: true)
        return exitCode == 0
    }

    @Override
    List<String> validateEnvironment() {
        List<String> errors = []
        
        if (!script.env.ANDROID_HOME) {
            errors << "ANDROID_HOME not set"
        }
        
        if (!script.fileExists('android/gradlew')) {
            errors << "Gradle wrapper not found"
        }
        
        return errors
    }

    /**
     * Find built artifact (APK or AAB).
     */
    private String findArtifact(BuildConfig config) {
        String pattern = config.outputType == 'aab' ? '**/*.aab' : '**/*.apk'
        def files = script.findFiles(glob: "android/app/build/outputs/${pattern}")
        
        // Filter out unsigned/test APKs
        files = files.findAll { f -> 
            !f.path.contains('unsigned') && !f.path.contains('androidTest')
        }
        
        return files ? files[0].path : null
    }

    static AndroidBuilder create(Object script) {
        return new AndroidBuilder(script)
    }
}
