package jenkins.pipeline.exceptions

/**
 * Exception thrown when test execution fails.
 */
class TestException extends PipelineException {

    private static final long serialVersionUID = 1L

    /** Test framework that failed */
    final String framework
    
    /** Number of failed tests */
    final int failedCount
    
    /** Total tests executed */
    final int totalCount

    TestException(String message) {
        super(message, 'Test')
        this.framework = null
        this.failedCount = 0
        this.totalCount = 0
    }

    TestException(String message, String framework) {
        super(message, 'Test')
        this.framework = framework
        this.failedCount = 0
        this.totalCount = 0
    }

    TestException(String message, String framework, int failedCount, int totalCount) {
        super(message, null, 'Test',
              generateRemediation(framework),
              [framework: framework, failed: failedCount, total: totalCount])
        this.framework = framework
        this.failedCount = failedCount
        this.totalCount = totalCount
    }

    TestException(String message, Throwable cause) {
        super(message, cause, 'Test')
        this.framework = null
        this.failedCount = 0
        this.totalCount = 0
    }

    /**
     * Get the pass rate as a percentage.
     */
    double getPassRate() {
        if (totalCount <= 0) return 0.0
        return ((totalCount - failedCount) / totalCount) * 100.0
    }

    private static List<String> generateRemediation(String framework) {
        switch (framework?.toLowerCase()) {
            case 'jest':
                return [
                    'Run "npm test -- --verbose" locally to see detailed failures',
                    'Check for missing mock implementations',
                    'Verify test environment matches CI configuration'
                ]
            case 'appium':
                return [
                    'Verify Docker emulator is running and accessible',
                    'Check Appium server connection settings',
                    'Review element selectors if tests timeout',
                    'Ensure app is properly installed on emulator'
                ]
            default:
                return [
                    'Review test output for specific failure messages',
                    'Run failing tests locally to reproduce',
                    'Check for flaky tests and add retries if needed'
                ]
        }
    }
}
