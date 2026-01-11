package jenkins.pipeline.enums

/**
 * Represents the output type for Android builds.
 * Determines the artifact format produced by the build.
 */
enum OutputType implements Serializable {
    
    APK('apk', 'Android Package', 'assemble', '.apk'),
    AAB('aab', 'Android App Bundle', 'bundle', '.aab')

    /** Short identifier */
    final String id
    
    /** Human-readable description */
    final String description
    
    /** Gradle task prefix */
    final String taskPrefix
    
    /** File extension */
    final String extension

    private OutputType(String id, String description, String taskPrefix, String extension) {
        this.id = id
        this.description = description
        this.taskPrefix = taskPrefix
        this.extension = extension
    }

    /**
     * Get the output directory pattern for this type.
     * @param flavor Product flavor (optional)
     * @param buildType Build type
     * @return Relative path to output directory
     */
    String getOutputDir(String flavor, String buildType) {
        String variantDir = flavor ? "${flavor}/${buildType}" : buildType
        return "android/app/build/outputs/${id}/${variantDir}"
    }

    /**
     * Get the Gradle task for this output type.
     * @param flavor Product flavor (optional)
     * @param buildType Build type
     * @return Full Gradle task name
     */
    String getGradleTask(String flavor, String buildType) {
        String flavorPart = flavor ? flavor.capitalize() : ''
        String typePart = buildType.capitalize()
        return "${taskPrefix}${flavorPart}${typePart}"
    }

    /**
     * Get file pattern for finding output artifacts.
     * @return Glob pattern for finding files
     */
    String getFilePattern() {
        return "**/*${extension}"
    }

    /**
     * Parse string to OutputType.
     * @param value String value (case-insensitive)
     * @return OutputType enum
     */
    static OutputType fromString(String value) {
        if (!value) {
            return APK // Default to APK
        }
        try {
            return valueOf(value.toUpperCase())
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid output type: '${value}'. Valid values are: ${values()*.name().join(', ')}"
            )
        }
    }

    /**
     * Check if this output type is suitable for Play Store upload.
     * Google Play prefers AAB format.
     */
    boolean isPlayStorePreferred() {
        return this == AAB
    }

    @Override
    String toString() {
        return id
    }
}
