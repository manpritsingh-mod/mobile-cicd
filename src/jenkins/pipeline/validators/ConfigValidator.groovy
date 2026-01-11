package jenkins.pipeline.validators

import jenkins.pipeline.config.PipelineConfig
import jenkins.pipeline.config.BuildConfig
import jenkins.pipeline.enums.BuildType
import jenkins.pipeline.enums.Environment
import jenkins.pipeline.exceptions.PipelineException

/**
 * Validator for pipeline and build configurations.
 * Provides comprehensive validation with detailed error messages.
 */
class ConfigValidator implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script

    ConfigValidator(Object script) {
        this.script = script
    }

    /**
     * Validate pipeline configuration.
     * @param config Configuration to validate
     * @return List of validation errors (empty if valid)
     */
    List<String> validatePipelineConfig(PipelineConfig config) {
        List<String> errors = []

        // Required fields
        if (!config.appName?.trim()) {
            errors << "appName is required"
        }

        // Docker configuration
        if (!config.dockerRegistry?.trim()) {
            errors << "dockerRegistry is required"
        }
        if (!config.dockerImage?.trim()) {
            errors << "dockerImage is required"
        }

        // Deployment validation
        if (config.isReleaseBuild()) {
            if (!config.keystoreCredentialsId) {
                errors << "keystoreCredentialsId is required for release builds"
            }
            if (!config.keystorePasswordCredentialsId) {
                errors << "keystorePasswordCredentialsId is required for release builds"
            }
            if (!config.keyAlias?.trim()) {
                errors << "keyAlias is required for release builds"
            }
        }

        // Play Store deployment validation
        if (config.isDeploymentEnabled()) {
            if (!config.playStoreCredentialsId) {
                errors << "playStoreCredentialsId is required for Play Store deployment"
            }
        }

        // E2E test validation
        if (config.runE2ETests) {
            if (!config.emulatorUrl?.trim()) {
                errors << "emulatorUrl is required when E2E tests are enabled"
            }
        }

        return errors
    }

    /**
     * Validate build configuration.
     * @param config Configuration to validate
     * @return List of validation errors (empty if valid)
     */
    List<String> validateBuildConfig(BuildConfig config) {
        List<String> errors = []

        if (!config.appName?.trim()) {
            errors << "appName is required"
        }

        if (!config.version) {
            errors << "version is required"
        }

        if (config.buildType == BuildType.RELEASE && !config.environment) {
            errors << "environment is required for release builds"
        }

        return errors
    }

    /**
     * Validate build environment.
     * Checks for required tools and SDKs.
     * @return List of validation errors (empty if valid)
     */
    List<String> validateEnvironment() {
        List<String> errors = []

        // Check Java
        if (!commandExists('java')) {
            errors << "Java is not installed or not in PATH"
        }

        // Check Node.js
        if (!commandExists('node')) {
            errors << "Node.js is not installed or not in PATH"
        }

        // Check npm/yarn
        if (!commandExists('npm') && !commandExists('yarn')) {
            errors << "Neither npm nor yarn is installed"
        }

        // Check Gradle wrapper
        if (!fileExists('android/gradlew')) {
            errors << "Gradle wrapper not found at android/gradlew"
        }

        // Check Android SDK
        String androidHome = script.env.ANDROID_HOME ?: script.env.ANDROID_SDK_ROOT
        if (!androidHome) {
            errors << "ANDROID_HOME environment variable is not set"
        }

        // Check Fastlane
        if (!commandExists('fastlane')) {
            errors << "Fastlane is not installed"
        }

        return errors
    }

    /**
     * Validate Android project structure.
     * @return List of validation errors (empty if valid)
     */
    List<String> validateProjectStructure() {
        List<String> errors = []

        // Check package.json
        if (!fileExists('package.json')) {
            errors << "package.json not found in project root"
        }

        // Check android directory
        if (!fileExists('android')) {
            errors << "android directory not found"
        }

        // Check build.gradle
        if (!fileExists('android/app/build.gradle') && !fileExists('android/app/build.gradle.kts')) {
            errors << "android/app/build.gradle(.kts) not found"
        }

        // Check gradlew
        if (!fileExists('android/gradlew')) {
            errors << "android/gradlew not found"
        }

        return errors
    }

    /**
     * Validate and throw if errors found.
     * @param config Configuration to validate
     * @throws PipelineException if validation fails
     */
    void requireValidPipelineConfig(PipelineConfig config) {
        List<String> errors = validatePipelineConfig(config)
        if (errors) {
            throw new PipelineException(
                "Pipeline configuration is invalid",
                null,
                'Validation',
                ['Fix the following configuration errors:', *errors],
                [errorCount: errors.size()]
            )
        }
    }

    /**
     * Validate and throw if errors found.
     * @param config Configuration to validate
     * @throws PipelineException if validation fails
     */
    void requireValidBuildConfig(BuildConfig config) {
        List<String> errors = validateBuildConfig(config)
        if (errors) {
            throw new PipelineException(
                "Build configuration is invalid",
                null,
                'Validation',
                ['Fix the following configuration errors:', *errors],
                [errorCount: errors.size()]
            )
        }
    }

    /**
     * Validate environment and throw if errors found.
     * @throws PipelineException if validation fails
     */
    void requireValidEnvironment() {
        List<String> errors = validateEnvironment()
        if (errors) {
            throw new PipelineException(
                "Build environment is not properly configured",
                null,
                'Validation',
                ['Ensure all required tools are installed:', *errors],
                [errorCount: errors.size()]
            )
        }
    }

    /**
     * Check if a command exists.
     */
    private boolean commandExists(String command) {
        try {
            int exitCode = script.sh(
                script: "command -v ${command} >/dev/null 2>&1",
                returnStatus: true
            )
            return exitCode == 0
        } catch (Exception e) {
            return false
        }
    }

    /**
     * Check if a file exists.
     */
    private boolean fileExists(String path) {
        return script.fileExists(path)
    }

    /**
     * Create from pipeline script.
     */
    static ConfigValidator create(Object script) {
        return new ConfigValidator(script)
    }
}
