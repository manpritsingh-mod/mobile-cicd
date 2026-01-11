package jenkins.pipeline.testing

import jenkins.pipeline.interfaces.ITestRunner
import jenkins.pipeline.logging.PipelineLogger

/**
 * Simple Jest test runner.
 */
class JestRunner implements ITestRunner, Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final boolean coverage

    JestRunner(Object script, boolean coverage = true) {
        this.script = script
        this.logger = PipelineLogger.create(script)
        this.coverage = coverage
    }

    @Override
    Map runTests() {
        logger.section("Running Jest Tests")
        long startTime = System.currentTimeMillis()
        
        // Build command
        String cmd = 'npm test -- --ci --json --outputFile=test-results/jest.json'
        if (coverage) {
            cmd += ' --coverage'
        }
        
        // Run tests
        int exitCode = script.sh(script: "mkdir -p test-results && ${cmd}", returnStatus: true)
        
        long duration = System.currentTimeMillis() - startTime
        
        // Parse results if file exists
        Map results = [
            success: exitCode == 0,
            framework: 'Jest',
            duration: duration
        ]
        
        if (script.fileExists('test-results/jest.json')) {
            try {
                def json = script.readJSON(file: 'test-results/jest.json')
                results.total = json.numTotalTests ?: 0
                results.passed = json.numPassedTests ?: 0
                results.failed = json.numFailedTests ?: 0
            } catch (Exception e) {
                logger.warn("Could not parse Jest results: ${e.message}")
            }
        }
        
        if (results.success) {
            logger.success("Tests passed: ${results.passed}/${results.total}")
        } else {
            logger.error("Tests failed: ${results.failed} failures")
        }
        
        return results
    }

    @Override
    boolean isAvailable() {
        return script.fileExists('package.json')
    }

    @Override
    String getFrameworkName() {
        return 'Jest'
    }

    static JestRunner create(Object script) {
        return new JestRunner(script)
    }
}
