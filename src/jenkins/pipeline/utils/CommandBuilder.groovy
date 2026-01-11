package jenkins.pipeline.utils

import groovy.transform.CompileStatic

/**
 * Fluent builder for constructing shell commands.
 * Provides safe escaping and option handling.
 */
@CompileStatic
class CommandBuilder implements Serializable {

    private static final long serialVersionUID = 1L

    private String executable
    private List<String> arguments = []
    private Map<String, String> environment = [:]
    private String workingDir
    private boolean sudo = false
    private String redirectOutput
    private String redirectError
    private boolean background = false

    private CommandBuilder(String executable) {
        this.executable = executable
    }

    /**
     * Create a new command builder.
     * @param executable Command to execute
     */
    static CommandBuilder create(String executable) {
        return new CommandBuilder(executable)
    }

    /**
     * Create a Gradle command builder.
     */
    static CommandBuilder gradle() {
        return new CommandBuilder('./gradlew')
    }

    /**
     * Create an npm command builder.
     */
    static CommandBuilder npm() {
        return new CommandBuilder('npm')
    }

    /**
     * Create a yarn command builder.
     */
    static CommandBuilder yarn() {
        return new CommandBuilder('yarn')
    }

    /**
     * Create a Fastlane command builder.
     */
    static CommandBuilder fastlane() {
        return new CommandBuilder('bundle exec fastlane')
    }

    /**
     * Add a positional argument.
     */
    CommandBuilder arg(String argument) {
        if (argument?.trim()) {
            arguments << argument.trim()
        }
        return this
    }

    /**
     * Add multiple arguments.
     */
    CommandBuilder args(String... args) {
        args?.each { arg(it) }
        return this
    }

    /**
     * Add multiple arguments from list.
     */
    CommandBuilder args(List<String> args) {
        args?.each { arg(it) }
        return this
    }

    /**
     * Add a flag (--flag).
     */
    CommandBuilder flag(String name) {
        if (name?.trim()) {
            String cleanName = name.startsWith('--') ? name : "--${name}"
            arguments << cleanName
        }
        return this
    }

    /**
     * Add a flag with value (--flag=value or --flag value).
     */
    CommandBuilder option(String name, Object value, boolean useEquals = true) {
        if (name?.trim() && value != null) {
            String cleanName = name.startsWith('--') ? name : "--${name}"
            String cleanValue = escapeValue(value.toString())
            
            if (useEquals) {
                arguments << "${cleanName}=${cleanValue}"
            } else {
                arguments << cleanName
                arguments << cleanValue
            }
        }
        return this
    }

    /**
     * Add a short option (-o value).
     */
    CommandBuilder shortOption(String name, Object value) {
        if (name?.trim() && value != null) {
            String cleanName = name.startsWith('-') ? name : "-${name}"
            arguments << cleanName
            arguments << escapeValue(value.toString())
        }
        return this
    }

    /**
     * Add Gradle property (-P).
     */
    CommandBuilder gradleProperty(String name, Object value) {
        if (name?.trim() && value != null) {
            arguments << "-P${name}=${escapeValue(value.toString())}"
        }
        return this
    }

    /**
     * Add environment variable.
     */
    CommandBuilder env(String name, String value) {
        if (name?.trim()) {
            environment[name] = value ?: ''
        }
        return this
    }

    /**
     * Add multiple environment variables.
     */
    CommandBuilder envs(Map<String, String> vars) {
        vars?.each { name, value -> env(name, value) }
        return this
    }

    /**
     * Set working directory.
     */
    CommandBuilder inDir(String directory) {
        this.workingDir = directory
        return this
    }

    /**
     * Run with sudo.
     */
    CommandBuilder withSudo() {
        this.sudo = true
        return this
    }

    /**
     * Redirect stdout to file.
     */
    CommandBuilder stdout(String file) {
        this.redirectOutput = file
        return this
    }

    /**
     * Redirect stderr to file.
     */
    CommandBuilder stderr(String file) {
        this.redirectError = file
        return this
    }

    /**
     * Run in background.
     */
    CommandBuilder inBackground() {
        this.background = true
        return this
    }

    /**
     * Build the command string.
     */
    String build() {
        StringBuilder sb = new StringBuilder()

        // Environment variables
        environment.each { name, value ->
            sb.append("${name}='${escapeValue(value)}' ")
        }

        // Sudo
        if (sudo) {
            sb.append('sudo ')
        }

        // Working directory
        if (workingDir) {
            sb.append("cd '${workingDir}' && ")
        }

        // Executable and arguments
        sb.append(executable)
        arguments.each { arg ->
            sb.append(" ${arg}")
        }

        // Redirects
        if (redirectOutput) {
            sb.append(" > '${redirectOutput}'")
        }
        if (redirectError) {
            sb.append(" 2> '${redirectError}'")
        }

        // Background
        if (background) {
            sb.append(' &')
        }

        return sb.toString()
    }

    /**
     * Build command as list (for ProcessBuilder).
     */
    List<String> buildAsList() {
        List<String> parts = []
        if (sudo) {
            parts << 'sudo'
        }
        parts.addAll(executable.split('\\s+').toList())
        parts.addAll(arguments)
        return parts
    }

    /**
     * Escape value for shell.
     */
    private String escapeValue(String value) {
        if (!value) return "''"
        // If already quoted, return as-is
        if ((value.startsWith("'") && value.endsWith("'")) ||
            (value.startsWith('"') && value.endsWith('"'))) {
            return value
        }
        // Quote if contains special characters
        if (value =~ /[\s'"$!`\\]/) {
            return "'${value.replace("'", "'\\''")}'"
        }
        return value
    }

    @Override
    String toString() {
        return build()
    }
}
