package jenkins.pipeline.config

import groovy.transform.ToString

/**
 * Android-specific build configuration.
 * Contains SDK paths, Gradle settings, and signing config.
 */
@ToString(includeNames = true, includePackage = false)
class AndroidConfig implements Serializable {

    private static final long serialVersionUID = 1L

    /** Android SDK root path */
    final String sdkRoot
    
    /** Compile SDK version */
    final int compileSdkVersion
    
    /** Min SDK version */
    final int minSdkVersion
    
    /** Target SDK version */
    final int targetSdkVersion
    
    /** Build tools version */
    final String buildToolsVersion
    
    /** NDK version (optional) */
    final String ndkVersion
    
    /** Gradle version */
    final String gradleVersion
    
    /** Gradle JVM args */
    final String gradleJvmArgs
    
    /** Gradle daemon enabled */
    final boolean gradleDaemon
    
    /** Parallel build enabled */
    final boolean parallelBuild
    
    /** Configuration cache enabled */
    final boolean configurationCache
    
    /** Extra Gradle properties */
    final Map<String, String> gradleProperties

    private AndroidConfig(Builder builder) {
        this.sdkRoot = builder.sdkRoot ?: System.getenv('ANDROID_HOME') ?: '/home/jenkins/android-sdk'
        this.compileSdkVersion = builder.compileSdkVersion ?: 34
        this.minSdkVersion = builder.minSdkVersion ?: 24
        this.targetSdkVersion = builder.targetSdkVersion ?: 34
        this.buildToolsVersion = builder.buildToolsVersion ?: '34.0.0'
        this.ndkVersion = builder.ndkVersion
        this.gradleVersion = builder.gradleVersion ?: '8.2'
        this.gradleJvmArgs = builder.gradleJvmArgs ?: '-Xmx4g -XX:+HeapDumpOnOutOfMemoryError'
        this.gradleDaemon = builder.gradleDaemon
        this.parallelBuild = builder.parallelBuild
        this.configurationCache = builder.configurationCache
        this.gradleProperties = builder.gradleProperties?.asImmutable() ?: [:]
    }

    /**
     * Get environment variables for Android build.
     */
    Map<String, String> getEnvironmentVariables() {
        return [
            'ANDROID_HOME': sdkRoot,
            'ANDROID_SDK_ROOT': sdkRoot,
            'PATH': "\${PATH}:${sdkRoot}/cmdline-tools/latest/bin:${sdkRoot}/platform-tools"
        ]
    }

    /**
     * Get Gradle command line arguments.
     */
    List<String> getGradleArgs() {
        List<String> args = []
        
        if (gradleDaemon) {
            args << '--daemon'
        } else {
            args << '--no-daemon'
        }
        
        if (parallelBuild) {
            args << '--parallel'
        }
        
        if (configurationCache) {
            args << '--configuration-cache'
        }
        
        // Add JVM args
        args << "-Dorg.gradle.jvmargs='${gradleJvmArgs}'"
        
        // Add custom properties
        gradleProperties.each { key, value ->
            args << "-P${key}=${value}"
        }
        
        return args
    }

    /**
     * Get full Gradle command with wrapper.
     * @param tasks List of Gradle tasks to execute
     * @return Full command string
     */
    String buildGradleCommand(List<String> tasks) {
        List<String> parts = ['./gradlew']
        parts.addAll(tasks)
        parts.addAll(gradleArgs)
        return parts.join(' ')
    }

    /**
     * Get SDK manager install command.
     * @param packages List of packages to install
     * @return Command string
     */
    String buildSdkManagerCommand(List<String> packages) {
        List<String> quotedPackages = packages.collect { "\"${it}\"" }
        return "sdkmanager ${quotedPackages.join(' ')}"
    }

    static Builder builder() {
        return new Builder()
    }

    /**
     * Create default Android config.
     */
    static AndroidConfig defaults() {
        return builder().build()
    }

    /**
     * Builder for AndroidConfig.
     */
    static class Builder implements Serializable {
        private static final long serialVersionUID = 1L

        String sdkRoot
        int compileSdkVersion = 34
        int minSdkVersion = 24
        int targetSdkVersion = 34
        String buildToolsVersion = '34.0.0'
        String ndkVersion
        String gradleVersion = '8.2'
        String gradleJvmArgs = '-Xmx4g -XX:+HeapDumpOnOutOfMemoryError'
        boolean gradleDaemon = true
        boolean parallelBuild = true
        boolean configurationCache = false
        Map<String, String> gradleProperties = [:]

        Builder sdkRoot(String path) { this.sdkRoot = path; return this }
        Builder compileSdkVersion(int version) { this.compileSdkVersion = version; return this }
        Builder minSdkVersion(int version) { this.minSdkVersion = version; return this }
        Builder targetSdkVersion(int version) { this.targetSdkVersion = version; return this }
        Builder buildToolsVersion(String version) { this.buildToolsVersion = version; return this }
        Builder ndkVersion(String version) { this.ndkVersion = version; return this }
        Builder gradleVersion(String version) { this.gradleVersion = version; return this }
        Builder gradleJvmArgs(String args) { this.gradleJvmArgs = args; return this }
        Builder gradleDaemon(boolean enabled) { this.gradleDaemon = enabled; return this }
        Builder parallelBuild(boolean enabled) { this.parallelBuild = enabled; return this }
        Builder configurationCache(boolean enabled) { this.configurationCache = enabled; return this }
        Builder gradleProperty(String key, String value) { 
            this.gradleProperties[key] = value
            return this 
        }
        Builder gradleProperties(Map<String, String> props) { 
            this.gradleProperties = props ?: [:]
            return this 
        }

        AndroidConfig build() {
            return new AndroidConfig(this)
        }
    }
}
