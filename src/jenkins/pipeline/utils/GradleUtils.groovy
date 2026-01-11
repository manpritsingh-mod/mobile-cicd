package jenkins.pipeline.utils

import jenkins.pipeline.config.AndroidConfig
import jenkins.pipeline.enums.BuildType
import jenkins.pipeline.enums.OutputType

/**
 * Utility class for Gradle operations.
 */
class GradleUtils implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final AndroidConfig config

    GradleUtils(Object script, AndroidConfig config = null) {
        this.script = script
        this.config = config ?: AndroidConfig.defaults()
    }

    /**
     * Execute a Gradle task.
     * @param tasks Tasks to execute
     * @param options Additional options
     * @return Exit code
     */
    int execute(List<String> tasks, Map<String, Object> options = [:]) {
        String command = buildCommand(tasks, options)
        
        script.dir('android') {
            return script.sh(
                script: command,
                returnStatus: true
            )
        }
    }

    /**
     * Execute Gradle and return output.
     * @param tasks Tasks to execute
     * @return Command output
     */
    String executeWithOutput(List<String> tasks) {
        String command = buildCommand(tasks, [:])
        
        return script.dir('android') {
            return script.sh(
                script: command,
                returnStdout: true
            ).trim()
        }
    }

    /**
     * Build command string.
     */
    String buildCommand(List<String> tasks, Map<String, Object> options) {
        CommandBuilder builder = CommandBuilder.gradle()
            .args(tasks)
        
        // Add config args
        config.gradleArgs.each { builder.arg(it) }
        
        // Add extra options
        if (options.stacktrace) {
            builder.flag('stacktrace')
        }
        
        if (options.info) {
            builder.flag('info')
        }
        
        if (options.debug) {
            builder.flag('debug')
        }
        
        options.properties?.each { key, value ->
            builder.gradleProperty(key, value)
        }
        
        return builder.build()
    }

    /**
     * Clean build directory.
     * @return true if successful
     */
    boolean clean() {
        int exitCode = execute(['clean'])
        return exitCode == 0
    }

    /**
     * Assemble build.
     * @param buildType Build type
     * @param flavor Product flavor (optional)
     * @return true if successful
     */
    boolean assemble(BuildType buildType, String flavor = null) {
        String task = buildType.getAssembleTask(flavor)
        int exitCode = execute([task])
        return exitCode == 0
    }

    /**
     * Bundle build (AAB).
     * @param buildType Build type
     * @param flavor Product flavor (optional)
     * @return true if successful
     */
    boolean bundle(BuildType buildType, String flavor = null) {
        String task = buildType.getBundleTask(flavor)
        int exitCode = execute([task])
        return exitCode == 0
    }

    /**
     * Run unit tests.
     * @param buildType Build type
     * @param flavor Product flavor (optional)
     * @return true if successful
     */
    boolean test(BuildType buildType, String flavor = null) {
        String task = buildType.getTestTask(flavor)
        int exitCode = execute([task])
        return exitCode == 0
    }

    /**
     * Run lint.
     * @param flavor Product flavor (optional)
     * @return true if successful
     */
    boolean lint(String flavor = null) {
        String task = flavor ? "lint${flavor.capitalize()}" : 'lint'
        int exitCode = execute([task])
        return exitCode == 0
    }

    /**
     * Get dependencies.
     * @return true if successful
     */
    boolean dependencies() {
        int exitCode = execute(['dependencies'])
        return exitCode == 0
    }

    /**
     * Get current version from build.gradle.
     * @return Version string
     */
    String getVersionName() {
        String output = executeWithOutput(['printVersionName', '-q'])
        return output?.trim()
    }

    /**
     * Get current version code from build.gradle.
     * @return Version code
     */
    int getVersionCode() {
        String output = executeWithOutput(['printVersionCode', '-q'])
        return output?.trim() ? Integer.parseInt(output.trim()) : 0
    }

    /**
     * Find output artifact.
     * @param outputType APK or AAB
     * @param buildType Build type
     * @param flavor Product flavor
     * @return Path to artifact
     */
    String findArtifact(OutputType outputType, BuildType buildType, String flavor = null) {
        FileUtils fileUtils = FileUtils.create(script)
        String outputDir = outputType.getOutputDir(flavor, buildType.gradleName)
        
        if (outputType == OutputType.APK) {
            return fileUtils.findApk(outputDir)
        } else {
            return fileUtils.findAab(outputDir)
        }
    }

    /**
     * Create from pipeline script.
     */
    static GradleUtils create(Object script, AndroidConfig config = null) {
        return new GradleUtils(script, config)
    }
}
