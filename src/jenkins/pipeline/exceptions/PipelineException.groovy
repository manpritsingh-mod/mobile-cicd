package jenkins.pipeline.exceptions

/**
 * Base exception class for all pipeline-related errors.
 * Provides consistent error handling across the shared library.
 */
class PipelineException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L

    /** Stage where the exception occurred */
    final String stageName
    
    /** Suggested remediation steps */
    final List<String> remediation
    
    /** Additional context data */
    final Map<String, Object> context

    PipelineException(String message) {
        this(message, null, null, [], [:])
    }

    PipelineException(String message, Throwable cause) {
        this(message, cause, null, [], [:])
    }

    PipelineException(String message, String stageName) {
        this(message, null, stageName, [], [:])
    }

    PipelineException(String message, Throwable cause, String stageName, 
                      List<String> remediation = [], Map<String, Object> context = [:]) {
        super(message, cause)
        this.stageName = stageName
        this.remediation = remediation ?: []
        this.context = context ?: [:]
    }

    /**
     * Get a formatted error report.
     * @return Multi-line error report string
     */
    String getFormattedReport() {
        StringBuilder sb = new StringBuilder()
        sb.append("╔══════════════════════════════════════════════════════════════╗\n")
        sb.append("║  PIPELINE ERROR                                              ║\n")
        sb.append("╠══════════════════════════════════════════════════════════════╣\n")
        sb.append("║  Type: ${this.class.simpleName.padRight(52)}║\n")
        
        if (stageName) {
            sb.append("║  Stage: ${stageName.padRight(51)}║\n")
        }
        
        sb.append("╠══════════════════════════════════════════════════════════════╣\n")
        sb.append("║  Message:                                                    ║\n")
        
        // Word wrap message
        wrapText(message, 60).each { line ->
            sb.append("║    ${line.padRight(58)}║\n")
        }
        
        if (remediation) {
            sb.append("╠══════════════════════════════════════════════════════════════╣\n")
            sb.append("║  Remediation:                                                ║\n")
            remediation.eachWithIndex { step, idx ->
                wrapText("${idx + 1}. ${step}", 58).each { line ->
                    sb.append("║    ${line.padRight(58)}║\n")
                }
            }
        }
        
        if (context) {
            sb.append("╠══════════════════════════════════════════════════════════════╣\n")
            sb.append("║  Context:                                                    ║\n")
            context.each { key, value ->
                sb.append("║    ${key}: ${value.toString().take(45).padRight(45)}║\n")
            }
        }
        
        sb.append("╚══════════════════════════════════════════════════════════════╝")
        return sb.toString()
    }

    /**
     * Word wrap text to specified width.
     */
    private List<String> wrapText(String text, int width) {
        if (!text || text.length() <= width) {
            return [text ?: '']
        }
        
        List<String> lines = []
        String remaining = text
        
        while (remaining.length() > width) {
            int breakPoint = remaining.lastIndexOf(' ', width)
            if (breakPoint <= 0) breakPoint = width
            lines << remaining.substring(0, breakPoint).trim()
            remaining = remaining.substring(breakPoint).trim()
        }
        
        if (remaining) {
            lines << remaining
        }
        
        return lines
    }

    @Override
    String toString() {
        return getFormattedReport()
    }
}
