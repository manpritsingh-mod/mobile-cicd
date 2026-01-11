package jenkins.pipeline.models

import groovy.transform.ToString

/**
 * Represents the result of test execution.
 * Aggregates results from different test frameworks.
 */
@ToString(includeNames = true, includePackage = false)
class TestResult implements Serializable {

    private static final long serialVersionUID = 1L

    /** Test framework name */
    final String framework
    
    /** Total number of tests */
    final int totalTests
    
    /** Number of passed tests */
    final int passedTests
    
    /** Number of failed tests */
    final int failedTests
    
    /** Number of skipped tests */
    final int skippedTests
    
    /** Execution time in milliseconds */
    final long durationMs
    
    /** Path to test report file */
    final String reportPath
    
    /** Coverage percentage (0-100) */
    final Double coveragePercent
    
    /** List of failed test names */
    final List<String> failedTestNames
    
    /** Error output if execution failed */
    final String errorOutput

    private TestResult(Builder builder) {
        this.framework = builder.framework
        this.totalTests = builder.totalTests
        this.passedTests = builder.passedTests
        this.failedTests = builder.failedTests
        this.skippedTests = builder.skippedTests
        this.durationMs = builder.durationMs
        this.reportPath = builder.reportPath
        this.coveragePercent = builder.coveragePercent
        this.failedTestNames = builder.failedTestNames?.asImmutable() ?: []
        this.errorOutput = builder.errorOutput
    }

    /**
     * Check if all tests passed.
     */
    boolean isSuccess() {
        return failedTests == 0 && !errorOutput
    }

    /**
     * Get pass rate as percentage.
     */
    double getPassRate() {
        if (totalTests <= 0) return 0.0
        return (passedTests / totalTests) * 100.0
    }

    /**
     * Get formatted duration.
     */
    String getFormattedDuration() {
        long totalSeconds = durationMs / 1000
        long minutes = totalSeconds / 60
        long seconds = totalSeconds % 60
        
        if (minutes > 0) {
            return "${minutes}m ${seconds}s"
        }
        return "${seconds}s"
    }

    /**
     * Get summary status string.
     */
    String getSummary() {
        if (errorOutput) {
            return "❌ Execution failed: ${errorOutput.take(100)}"
        }
        
        if (isSuccess()) {
            return "✅ All ${totalTests} tests passed in ${formattedDuration}"
        }
        
        return "❌ ${failedTests}/${totalTests} tests failed"
    }

    /**
     * Get coverage status for display.
     */
    String getCoverageStatus() {
        if (coveragePercent == null) {
            return "Coverage: N/A"
        }
        
        String icon = coveragePercent >= 80 ? "🟢" : 
                      coveragePercent >= 60 ? "🟡" : "🔴"
        
        return "${icon} Coverage: ${String.format('%.1f', coveragePercent)}%"
    }

    /**
     * Merge multiple test results.
     */
    static TestResult merge(String framework, List<TestResult> results) {
        if (!results) {
            return new Builder().framework(framework).build()
        }

        int total = results.sum { it.totalTests } as int
        int passed = results.sum { it.passedTests } as int
        int failed = results.sum { it.failedTests } as int
        int skipped = results.sum { it.skippedTests } as int
        long duration = results.sum { it.durationMs } as long
        List<String> allFailed = results.collectMany { it.failedTestNames }
        String errors = results.findAll { it.errorOutput }
                              .collect { it.errorOutput }
                              .join('\n')

        return new Builder()
            .framework(framework)
            .totalTests(total)
            .passedTests(passed)
            .failedTests(failed)
            .skippedTests(skipped)
            .durationMs(duration)
            .failedTestNames(allFailed)
            .errorOutput(errors ?: null)
            .build()
    }

    static Builder builder() {
        return new Builder()
    }

    /**
     * Builder for TestResult.
     */
    static class Builder implements Serializable {
        private static final long serialVersionUID = 1L

        String framework = "Unknown"
        int totalTests = 0
        int passedTests = 0
        int failedTests = 0
        int skippedTests = 0
        long durationMs = 0
        String reportPath
        Double coveragePercent
        List<String> failedTestNames = []
        String errorOutput

        Builder framework(String framework) {
            this.framework = framework
            return this
        }

        Builder totalTests(int count) {
            this.totalTests = count
            return this
        }

        Builder passedTests(int count) {
            this.passedTests = count
            return this
        }

        Builder failedTests(int count) {
            this.failedTests = count
            return this
        }

        Builder skippedTests(int count) {
            this.skippedTests = count
            return this
        }

        Builder durationMs(long duration) {
            this.durationMs = duration
            return this
        }

        Builder reportPath(String path) {
            this.reportPath = path
            return this
        }

        Builder coveragePercent(Double percent) {
            this.coveragePercent = percent
            return this
        }

        Builder failedTestNames(List<String> names) {
            this.failedTestNames = names ?: []
            return this
        }

        Builder addFailedTest(String testName) {
            this.failedTestNames << testName
            return this
        }

        Builder errorOutput(String error) {
            this.errorOutput = error
            return this
        }

        TestResult build() {
            // Auto-calculate passed if not set
            if (passedTests == 0 && totalTests > 0 && failedTests + skippedTests <= totalTests) {
                passedTests = totalTests - failedTests - skippedTests
            }
            return new TestResult(this)
        }
    }
}
