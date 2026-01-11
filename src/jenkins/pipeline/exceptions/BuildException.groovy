package jenkins.pipeline.exceptions

/**
 * Exception thrown when a build operation fails.
 */
class BuildException extends PipelineException {

    private static final long serialVersionUID = 1L

    /** Exit code from build command */
    final int exitCode
    
    /** Build command that failed */
    final String command

    BuildException(String message) {
        super(message, 'Build')
        this.exitCode = -1
        this.command = null
    }

    BuildException(String message, int exitCode) {
        super(message, 'Build')
        this.exitCode = exitCode
        this.command = null
    }

    BuildException(String message, String command, int exitCode) {
        super(message, null, 'Build', 
              generateRemediation(exitCode),
              [command: command, exitCode: exitCode])
        this.exitCode = exitCode
        this.command = command
    }

    BuildException(String message, Throwable cause) {
        super(message, cause, 'Build')
        this.exitCode = -1
        this.command = null
    }

    private static List<String> generateRemediation(int exitCode) {
        switch (exitCode) {
            case 1:
                return [
                    'Check Gradle build logs for compilation errors',
                    'Verify all dependencies are correctly specified',
                    'Run "./gradlew clean" and try again'
                ]
            case 137:
                return [
                    'Build was killed due to memory issues',
                    'Increase Java heap size: -Xmx4g',
                    'Consider using Gradle daemon memory settings'
                ]
            default:
                return [
                    'Review build logs for detailed error messages',
                    'Verify environment variables are set correctly',
                    'Ensure all SDK components are installed'
                ]
        }
    }
}
