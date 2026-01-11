package jenkins.pipeline.models

import groovy.transform.ToString
import jenkins.pipeline.enums.BuildType
import jenkins.pipeline.enums.OutputType

/**
 * Represents the result of a build operation.
 * Contains artifact paths, timing information, and status.
 */
@ToString(includeNames = true, includePackage = false)
class BuildResult implements Serializable {

    private static final long serialVersionUID = 1L

    /** Build was successful */
    final boolean success
    
    /** Path to the primary artifact (APK/AAB) */
    final String artifactPath
    
    /** Build type used */
    final BuildType buildType
    
    /** Output type produced */
    final OutputType outputType
    
    /** Full variant name */
    final String variantName
    
    /** App version */
    final AppVersion version
    
    /** Build start time in milliseconds */
    final long startTime
    
    /** Build end time in milliseconds */
    final long endTime
    
    /** File size in bytes */
    final long fileSize
    
    /** Error message if failed */
    final String errorMessage
    
    /** Additional metadata */
    final Map<String, Object> metadata

    private BuildResult(Builder builder) {
        this.success = builder.success
        this.artifactPath = builder.artifactPath
        this.buildType = builder.buildType
        this.outputType = builder.outputType
        this.variantName = builder.variantName
        this.version = builder.version
        this.startTime = builder.startTime
        this.endTime = builder.endTime
        this.fileSize = builder.fileSize
        this.errorMessage = builder.errorMessage
        this.metadata = builder.metadata?.asImmutable() ?: [:]
    }

    /**
     * Get build duration in milliseconds.
     */
    long getDurationMs() {
        return endTime - startTime
    }

    /**
     * Get formatted duration string (e.g., "2m 35s").
     */
    String getFormattedDuration() {
        long totalSeconds = getDurationMs() / 1000
        long minutes = totalSeconds / 60
        long seconds = totalSeconds % 60
        
        if (minutes > 0) {
            return "${minutes}m ${seconds}s"
        }
        return "${seconds}s"
    }

    /**
     * Get human-readable file size.
     */
    String getFormattedFileSize() {
        if (fileSize <= 0) return "Unknown"
        
        double size = fileSize
        String[] units = ['B', 'KB', 'MB', 'GB']
        int unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", size, units[unitIndex])
    }

    /**
     * Get artifact filename.
     */
    String getArtifactFileName() {
        if (!artifactPath) return null
        return new File(artifactPath).name
    }

    /**
     * Create success result.
     */
    static BuildResult success(String artifactPath, BuildConfig config, long startTime) {
        return new Builder()
            .success(true)
            .artifactPath(artifactPath)
            .buildType(config.buildType)
            .outputType(config.outputType)
            .variantName(config.variantName)
            .version(config.version)
            .startTime(startTime)
            .endTime(System.currentTimeMillis())
            .build()
    }

    /**
     * Create failure result.
     */
    static BuildResult failure(String errorMessage, BuildConfig config, long startTime) {
        return new Builder()
            .success(false)
            .errorMessage(errorMessage)
            .buildType(config?.buildType)
            .outputType(config?.outputType)
            .variantName(config?.variantName)
            .version(config?.version)
            .startTime(startTime)
            .endTime(System.currentTimeMillis())
            .build()
    }

    static Builder builder() {
        return new Builder()
    }

    /**
     * Builder for BuildResult.
     */
    static class Builder implements Serializable {
        private static final long serialVersionUID = 1L

        boolean success = false
        String artifactPath
        BuildType buildType
        OutputType outputType
        String variantName
        AppVersion version
        long startTime = System.currentTimeMillis()
        long endTime = System.currentTimeMillis()
        long fileSize = 0
        String errorMessage
        Map<String, Object> metadata = [:]

        Builder success(boolean success) {
            this.success = success
            return this
        }

        Builder artifactPath(String path) {
            this.artifactPath = path
            return this
        }

        Builder buildType(BuildType type) {
            this.buildType = type
            return this
        }

        Builder outputType(OutputType type) {
            this.outputType = type
            return this
        }

        Builder variantName(String name) {
            this.variantName = name
            return this
        }

        Builder version(AppVersion version) {
            this.version = version
            return this
        }

        Builder startTime(long time) {
            this.startTime = time
            return this
        }

        Builder endTime(long time) {
            this.endTime = time
            return this
        }

        Builder fileSize(long size) {
            this.fileSize = size
            return this
        }

        Builder errorMessage(String message) {
            this.errorMessage = message
            return this
        }

        Builder metadata(String key, Object value) {
            this.metadata[key] = value
            return this
        }

        Builder metadata(Map<String, Object> meta) {
            this.metadata = meta ?: [:]
            return this
        }

        BuildResult build() {
            return new BuildResult(this)
        }
    }
}
