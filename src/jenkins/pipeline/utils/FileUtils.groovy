package jenkins.pipeline.utils

/**
 * Utility class for file operations in Jenkins pipeline.
 */
class FileUtils implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script

    FileUtils(Object script) {
        this.script = script
    }

    /**
     * Find files matching a pattern.
     * @param baseDir Base directory to search
     * @param pattern Glob pattern
     * @return List of matching file paths
     */
    List<String> findFiles(String baseDir, String pattern) {
        def files = script.findFiles(glob: "${baseDir}/${pattern}")
        return files.collect { it.path }
    }

    /**
     * Find the first APK file in the output directory.
     * @param baseDir Build output directory
     * @return Path to APK file or null
     */
    String findApk(String baseDir) {
        List<String> apks = findFiles(baseDir, '**/*.apk')
        // Filter out unsigned and test APKs
        apks = apks.findAll { path ->
            !path.contains('unsigned') && !path.contains('androidTest')
        }
        return apks ? apks.first() : null
    }

    /**
     * Find the first AAB file in the output directory.
     * @param baseDir Build output directory
     * @return Path to AAB file or null
     */
    String findAab(String baseDir) {
        List<String> aabs = findFiles(baseDir, '**/*.aab')
        return aabs ? aabs.first() : null
    }

    /**
     * Get file size in bytes.
     * @param filePath Path to file
     * @return File size in bytes
     */
    long getFileSize(String filePath) {
        String result = script.sh(
            script: "stat -f%z '${filePath}' 2>/dev/null || stat -c%s '${filePath}'",
            returnStdout: true
        ).trim()
        return result ? Long.parseLong(result) : 0
    }

    /**
     * Check if file exists.
     * @param filePath Path to file
     * @return true if file exists
     */
    boolean fileExists(String filePath) {
        return script.fileExists(filePath)
    }

    /**
     * Read file contents.
     * @param filePath Path to file
     * @return File contents as string
     */
    String readFile(String filePath) {
        return script.readFile(file: filePath, encoding: 'UTF-8')
    }

    /**
     * Write content to file.
     * @param filePath Path to file
     * @param content Content to write
     */
    void writeFile(String filePath, String content) {
        script.writeFile(file: filePath, text: content, encoding: 'UTF-8')
    }

    /**
     * Create directory.
     * @param dirPath Directory path
     */
    void mkdir(String dirPath) {
        script.sh("mkdir -p '${dirPath}'")
    }

    /**
     * Delete file or directory.
     * @param path Path to delete
     */
    void delete(String path) {
        script.sh("rm -rf '${path}'")
    }

    /**
     * Copy file.
     * @param source Source path
     * @param destination Destination path
     */
    void copy(String source, String destination) {
        script.sh("cp -r '${source}' '${destination}'")
    }

    /**
     * Archive artifacts to Jenkins.
     * @param pattern Glob pattern for artifacts
     * @param allowEmpty Allow empty archive
     */
    void archiveArtifacts(String pattern, boolean allowEmpty = false) {
        script.archiveArtifacts(
            artifacts: pattern,
            allowEmptyArchive: allowEmpty,
            fingerprint: true
        )
    }

    /**
     * Stash files for later use in pipeline.
     * @param name Stash name
     * @param includes Include pattern
     */
    void stash(String name, String includes) {
        script.stash(name: name, includes: includes)
    }

    /**
     * Unstash previously stashed files.
     * @param name Stash name
     */
    void unstash(String name) {
        script.unstash(name: name)
    }

    /**
     * Read JSON file.
     * @param filePath Path to JSON file
     * @return Parsed JSON as Map
     */
    Map<String, Object> readJson(String filePath) {
        return script.readJSON(file: filePath)
    }

    /**
     * Write JSON file.
     * @param filePath Path to JSON file
     * @param data Data to write
     */
    void writeJson(String filePath, Map<String, Object> data) {
        script.writeJSON(file: filePath, json: data, pretty: 2)
    }

    /**
     * Get checksum of file.
     * @param filePath Path to file
     * @param algorithm Checksum algorithm (md5, sha256)
     * @return Checksum string
     */
    String getChecksum(String filePath, String algorithm = 'sha256') {
        String cmd = algorithm == 'md5' ? 'md5sum' : 'sha256sum'
        String result = script.sh(
            script: "${cmd} '${filePath}' | cut -d' ' -f1",
            returnStdout: true
        ).trim()
        return result
    }

    /**
     * Create from pipeline script.
     */
    static FileUtils create(Object script) {
        return new FileUtils(script)
    }
}
