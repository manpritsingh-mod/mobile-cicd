package jenkins.pipeline.models

/**
 * Simple build configuration.
 */
class BuildConfig implements Serializable {

    private static final long serialVersionUID = 1L

    String appName
    String version = '1.0.0'
    int versionCode = 1
    String buildType = 'debug'      // debug or release
    String environment = 'dev'      // dev, staging, prod
    String outputType = 'apk'       // apk or aab
    String flavor                   // optional product flavor
    boolean cleanBuild = true

    BuildConfig() {}

    BuildConfig(Map config) {
        this.appName = config.appName
        this.version = config.version ?: '1.0.0'
        this.versionCode = config.versionCode ?: 1
        this.buildType = config.buildType ?: 'debug'
        this.environment = config.environment ?: 'dev'
        this.outputType = config.outputType ?: 'apk'
        this.flavor = config.flavor
        this.cleanBuild = config.cleanBuild != false
    }

    /**
     * Get variant name like "devDebug" or "prodRelease"
     */
    String getVariantName() {
        String env = flavor ?: environment
        return "${env}${buildType.capitalize()}"
    }

    /**
     * Get Gradle task name
     */
    String getGradleTask() {
        String taskType = outputType == 'aab' ? 'bundle' : 'assemble'
        return "${taskType}${variantName.capitalize()}"
    }

    /**
     * Check if release build
     */
    boolean isRelease() {
        return buildType == 'release'
    }

    static BuildConfig create(Map config = [:]) {
        return new BuildConfig(config)
    }
}
