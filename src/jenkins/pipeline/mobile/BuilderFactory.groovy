package jenkins.pipeline.mobile

import jenkins.pipeline.enums.Platform
import jenkins.pipeline.interfaces.IBuilder

/**
 * Factory for creating platform-specific builders.
 * Implements Factory pattern for builder instantiation.
 */
class BuilderFactory implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private static final Map<Platform, Class<? extends IBuilder>> BUILDER_REGISTRY = [:]

    static {
        // Register default builders
        registerBuilder(Platform.ANDROID, AndroidBuilder)
    }

    BuilderFactory(Object script) {
        this.script = script
    }

    /**
     * Register a builder class for a platform.
     * @param platform Target platform
     * @param builderClass Builder implementation class
     */
    static void registerBuilder(Platform platform, Class<? extends IBuilder> builderClass) {
        BUILDER_REGISTRY[platform] = builderClass
    }

    /**
     * Create a builder for the specified platform.
     * @param platform Target platform
     * @return Builder instance
     * @throws IllegalArgumentException if no builder registered for platform
     */
    IBuilder createBuilder(Platform platform) {
        Class<? extends IBuilder> builderClass = BUILDER_REGISTRY[platform]
        
        if (!builderClass) {
            throw new IllegalArgumentException(
                "No builder registered for platform: ${platform}. " +
                "Available platforms: ${BUILDER_REGISTRY.keySet().join(', ')}"
            )
        }
        
        return builderClass.newInstance(script)
    }

    /**
     * Create an Android builder.
     * @return AndroidBuilder instance
     */
    AndroidBuilder createAndroidBuilder() {
        return new AndroidBuilder(script)
    }

    /**
     * Check if a platform is supported.
     * @param platform Platform to check
     * @return true if supported
     */
    boolean isSupported(Platform platform) {
        return BUILDER_REGISTRY.containsKey(platform)
    }

    /**
     * Get list of supported platforms.
     * @return List of supported platforms
     */
    List<Platform> getSupportedPlatforms() {
        return BUILDER_REGISTRY.keySet().toList()
    }

    /**
     * Create factory from pipeline script.
     */
    static BuilderFactory create(Object script) {
        return new BuilderFactory(script)
    }
}
