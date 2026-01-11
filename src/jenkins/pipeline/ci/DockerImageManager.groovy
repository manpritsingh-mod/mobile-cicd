package jenkins.pipeline.ci

import jenkins.pipeline.logging.PipelineLogger

/**
 * Manager for Docker image operations.
 * Handles pulling from Nexus registry and managing build containers.
 */
class DockerImageManager implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final String registryUrl
    private String currentImage

    DockerImageManager(Object script, String registryUrl = '43.88.89.25:5000') {
        this.script = script
        this.registryUrl = registryUrl
        this.logger = PipelineLogger.create(script)
    }

    /**
     * Pull a Docker image from the Nexus registry.
     * @param imageName Image name
     * @param tag Image tag
     * @return Full image path
     */
    String pullImage(String imageName, String tag = 'latest') {
        String fullImage = "${registryUrl}/${imageName}:${tag}"
        
        logger.section("Pulling Docker Image")
        logger.property("Image", fullImage)
        
        int exitCode = script.sh(
            script: "docker pull ${fullImage}",
            returnStatus: true
        )
        
        if (exitCode != 0) {
            throw new RuntimeException("Failed to pull Docker image: ${fullImage}")
        }
        
        this.currentImage = fullImage
        logger.success("Image pulled successfully")
        
        return fullImage
    }

    /**
     * Run build inside Docker container.
     * @param imageName Image to use
     * @param tag Image tag
     * @param closure Build steps to execute
     * @return Result of closure execution
     */
    def runInContainer(String imageName, String tag = 'latest', Closure closure) {
        String fullImage = "${registryUrl}/${imageName}:${tag}"
        
        logger.info("Running build in container: ${fullImage}")
        
        script.docker.image(fullImage).inside(getContainerArgs()) {
            closure()
        }
    }

    /**
     * Run with mounted volumes.
     * @param imageName Image to use
     * @param volumes Map of host:container volume mappings
     * @param closure Build steps to execute
     */
    def runWithVolumes(String imageName, Map<String, String> volumes, Closure closure) {
        String fullImage = "${registryUrl}/${imageName}:latest"
        String volumeArgs = volumes.collect { host, container -> 
            "-v '${host}:${container}'" 
        }.join(' ')
        
        script.docker.image(fullImage).inside("${getContainerArgs()} ${volumeArgs}") {
            closure()
        }
    }

    /**
     * Build a custom Docker image.
     * @param dockerfile Path to Dockerfile
     * @param imageName Name for the built image
     * @param tag Tag for the image
     * @param buildArgs Build arguments
     * @return Full image path
     */
    String buildImage(String dockerfile, String imageName, String tag = 'latest', 
                      Map<String, String> buildArgs = [:]) {
        String fullImage = "${registryUrl}/${imageName}:${tag}"
        
        logger.section("Building Docker Image")
        logger.property("Dockerfile", dockerfile)
        logger.property("Image", fullImage)
        
        StringBuilder cmd = new StringBuilder("docker build")
        cmd.append(" -t ${fullImage}")
        cmd.append(" -f ${dockerfile}")
        
        buildArgs.each { key, value ->
            cmd.append(" --build-arg ${key}='${value}'")
        }
        
        cmd.append(" .")
        
        int exitCode = script.sh(
            script: cmd.toString(),
            returnStatus: true
        )
        
        if (exitCode != 0) {
            throw new RuntimeException("Failed to build Docker image")
        }
        
        logger.success("Image built successfully")
        return fullImage
    }

    /**
     * Push image to Nexus registry.
     * @param imageName Full image path (including registry)
     */
    void pushImage(String imageName) {
        logger.info("Pushing image: ${imageName}")
        
        int exitCode = script.sh(
            script: "docker push ${imageName}",
            returnStatus: true
        )
        
        if (exitCode != 0) {
            throw new RuntimeException("Failed to push Docker image: ${imageName}")
        }
        
        logger.success("Image pushed successfully")
    }

    /**
     * Tag an existing image.
     * @param sourceImage Source image path
     * @param targetImage Target image path
     */
    void tagImage(String sourceImage, String targetImage) {
        script.sh("docker tag ${sourceImage} ${targetImage}")
    }

    /**
     * Remove local image.
     * @param imageName Image to remove
     */
    void removeImage(String imageName) {
        script.sh(
            script: "docker rmi ${imageName} || true",
            returnStatus: true
        )
    }

    /**
     * Get image info.
     * @param imageName Image to inspect
     * @return Image info map
     */
    Map<String, Object> getImageInfo(String imageName) {
        String output = script.sh(
            script: "docker inspect ${imageName} --format '{{json .}}'",
            returnStdout: true
        ).trim()
        
        return script.readJSON(text: output)
    }

    /**
     * Check if image exists locally.
     * @param imageName Image to check
     * @return true if exists
     */
    boolean imageExists(String imageName) {
        int exitCode = script.sh(
            script: "docker image inspect ${imageName} >/dev/null 2>&1",
            returnStatus: true
        )
        return exitCode == 0
    }

    /**
     * Prune unused images.
     */
    void pruneImages() {
        logger.info("Pruning unused Docker images")
        script.sh("docker image prune -f")
    }

    /**
     * Get standard container arguments.
     */
    private String getContainerArgs() {
        List<String> args = [
            '--network=host',
            '-u root',
            "-v ${script.env.WORKSPACE}:/workspace",
            '-w /workspace',
            '-e GRADLE_USER_HOME=/workspace/.gradle',
            '-e NPM_CONFIG_CACHE=/workspace/.npm'
        ]
        
        // Add Android SDK mount if available
        String androidHome = script.env.ANDROID_HOME ?: script.env.ANDROID_SDK_ROOT
        if (androidHome) {
            args << "-e ANDROID_HOME=${androidHome}"
            args << "-e ANDROID_SDK_ROOT=${androidHome}"
        }
        
        return args.join(' ')
    }

    /**
     * Create from pipeline script.
     */
    static DockerImageManager create(Object script, String registryUrl = '43.88.89.25:5000') {
        return new DockerImageManager(script, registryUrl)
    }
}
