package jenkins.pipeline.logging

import groovy.transform.CompileStatic

/**
 * Centralized logger for pipeline operations.
 * Provides consistent, colorized, and structured logging.
 * 
 * Uses singleton pattern to ensure consistent logging across pipeline.
 */
@CompileStatic
class PipelineLogger implements Serializable {

    private static final long serialVersionUID = 1L

    /** ANSI color codes for console output */
    static final class Colors {
        static final String RESET = '\u001B[0m'
        static final String RED = '\u001B[31m'
        static final String GREEN = '\u001B[32m'
        static final String YELLOW = '\u001B[33m'
        static final String BLUE = '\u001B[34m'
        static final String PURPLE = '\u001B[35m'
        static final String CYAN = '\u001B[36m'
        static final String WHITE = '\u001B[37m'
        static final String BOLD = '\u001B[1m'
    }

    /** Log level enum */
    enum Level {
        DEBUG(0, Colors.CYAN, '🔍'),
        INFO(1, Colors.GREEN, 'ℹ️'),
        WARN(2, Colors.YELLOW, '⚠️'),
        ERROR(3, Colors.RED, '❌'),
        SUCCESS(1, Colors.GREEN, '✅'),
        STAGE(1, Colors.PURPLE, '🚀')

        final int priority
        final String color
        final String icon

        Level(int priority, String color, String icon) {
            this.priority = priority
            this.color = color
            this.icon = icon
        }
    }

    /** Pipeline script context for echo */
    private final Object script
    
    /** Minimum log level to output */
    private Level minLevel = Level.INFO
    
    /** Current stage name */
    private String currentStage = ''
    
    /** Whether to use colors */
    private boolean useColors = true

    PipelineLogger(Object script) {
        this.script = script
    }

    /**
     * Set minimum log level.
     */
    PipelineLogger setLevel(Level level) {
        this.minLevel = level
        return this
    }

    /**
     * Set current stage name.
     */
    PipelineLogger setStage(String stageName) {
        this.currentStage = stageName
        return this
    }

    /**
     * Enable/disable colors.
     */
    PipelineLogger useColors(boolean enable) {
        this.useColors = enable
        return this
    }

    /**
     * Log debug message.
     */
    void debug(String message) {
        log(Level.DEBUG, message)
    }

    /**
     * Log info message.
     */
    void info(String message) {
        log(Level.INFO, message)
    }

    /**
     * Log warning message.
     */
    void warn(String message) {
        log(Level.WARN, message)
    }

    /**
     * Log error message.
     */
    void error(String message) {
        log(Level.ERROR, message)
    }

    /**
     * Log success message.
     */
    void success(String message) {
        log(Level.SUCCESS, message)
    }

    /**
     * Log stage start.
     */
    void stage(String stageName) {
        this.currentStage = stageName
        String separator = '═' * 60
        String output = """
${colorize(separator, Colors.PURPLE)}
${colorize("${Level.STAGE.icon} STAGE: ${stageName}", Colors.PURPLE + Colors.BOLD)}
${colorize(separator, Colors.PURPLE)}
"""
        echo(output)
    }

    /**
     * Log stage completion.
     */
    void stageComplete(String stageName, long durationMs = 0) {
        String duration = durationMs > 0 ? " (${formatDuration(durationMs)})" : ""
        success("Stage '${stageName}' completed${duration}")
    }

    /**
     * Log command execution.
     */
    void command(String cmd) {
        echo(colorize("$ ${cmd}", Colors.CYAN))
    }

    /**
     * Log a section header.
     */
    void section(String title) {
        String line = '─' * 50
        echo("""
${colorize(line, Colors.BLUE)}
${colorize("  ${title}", Colors.BLUE + Colors.BOLD)}
${colorize(line, Colors.BLUE)}
""")
    }

    /**
     * Log a key-value pair.
     */
    void property(String key, Object value) {
        echo("  ${colorize(key + ':', Colors.CYAN)} ${value}")
    }

    /**
     * Log a map of properties.
     */
    void properties(Map<String, Object> props) {
        props?.each { key, value ->
            property(key, value)
        }
    }

    /**
     * Log a list of items.
     */
    void list(List<String> items, String prefix = '  •') {
        items?.each { item ->
            echo("${prefix} ${item}")
        }
    }

    /**
     * Log a divider line.
     */
    void divider() {
        echo(colorize('─' * 60, Colors.WHITE))
    }

    /**
     * Core log method.
     */
    private void log(Level level, String message) {
        if (level.priority < minLevel.priority) {
            return
        }

        String timestamp = new Date().format('HH:mm:ss')
        String stagePrefix = currentStage ? "[${currentStage}] " : ""
        String output = "${level.icon} ${colorize(timestamp, Colors.WHITE)} ${stagePrefix}${message}"
        
        echo(output)
    }

    /**
     * Apply color to text if colors are enabled.
     */
    private String colorize(String text, String color) {
        if (!useColors) return text
        return "${color}${text}${Colors.RESET}"
    }

    /**
     * Format duration in human-readable format.
     */
    private String formatDuration(long ms) {
        long seconds = ms / 1000
        long minutes = seconds / 60
        seconds = seconds % 60
        
        if (minutes > 0) {
            return "${minutes}m ${seconds}s"
        }
        return "${seconds}s"
    }

    /**
     * Echo to Jenkins console.
     */
    private void echo(String message) {
        if (script?.respondsTo('echo')) {
            script.echo(message)
        } else {
            println(message)
        }
    }

    /**
     * Create logger from pipeline script.
     */
    static PipelineLogger create(Object script) {
        return new PipelineLogger(script)
    }
}
