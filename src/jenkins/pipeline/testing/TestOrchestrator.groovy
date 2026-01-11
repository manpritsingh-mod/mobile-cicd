package jenkins.pipeline.testing

import jenkins.pipeline.interfaces.ITestRunner
import jenkins.pipeline.logging.PipelineLogger
import jenkins.pipeline.models.TestResult

/**
 * Orchestrates multiple test runners.
 * Coordinates Jest unit tests and Appium E2E tests.
 */
class TestOrchestrator implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final List<ITestRunner> runners = []

    TestOrchestrator(Object script) {
        this.script = script
        this.logger = PipelineLogger.create(script)
    }

    /**
     * Add a test runner.
     */
    TestOrchestrator addRunner(ITestRunner runner) {
        if (runner?.isAvailable()) {
            runners << runner
            logger.debug("Added test runner: ${runner.frameworkName}")
        } else {
            logger.warn("Test runner not available: ${runner?.frameworkName}")
        }
        return this
    }

    /**
     * Add Jest runner.
     */
    TestOrchestrator withJest(boolean coverage = true) {
        addRunner(JestRunner.create(script, coverage))
        return this
    }

    /**
     * Add Appium runner.
     */
    TestOrchestrator withAppium(String emulatorUrl, String appPath = null) {
        AppiumRunner runner = AppiumRunner.create(script, emulatorUrl)
        if (appPath) {
            runner.withApp(appPath)
        }
        addRunner(runner)
        return this
    }

    /**
     * Run all configured tests.
     * @param options Options to pass to all runners
     * @return Combined test result
     */
    TestResult runAll(Map<String, Object> options = [:]) {
        logger.section("Running All Tests")
        
        if (runners.isEmpty()) {
            logger.warn("No test runners configured")
            return TestResult.builder()
                .framework('Combined')
                .errorOutput("No test runners configured")
                .build()
        }
        
        logger.info("Configured runners: ${runners.collect { it.frameworkName }.join(', ')}")
        
        List<TestResult> results = []
        
        runners.each { runner ->
            logger.info("Running ${runner.frameworkName} tests...")
            
            try {
                runner.prepareEnvironment()
                TestResult result = runner.runTests(options)
                results << result
                
                logger.info("${runner.frameworkName}: ${result.summary}")
                
            } catch (Exception e) {
                logger.error("${runner.frameworkName} failed: ${e.message}")
                results << TestResult.builder()
                    .framework(runner.frameworkName)
                    .errorOutput(e.message)
                    .build()
            } finally {
                runner.cleanupEnvironment()
            }
        }
        
        // Merge all results
        TestResult combined = TestResult.merge('Combined', results)
        
        logger.divider()
        logger.info("Combined Results: ${combined.summary}")
        
        return combined
    }

    /**
     * Run only unit tests (Jest).
     */
    TestResult runUnitTests(Map<String, Object> options = [:]) {
        ITestRunner jestRunner = runners.find { it.frameworkName == 'Jest' }
        
        if (!jestRunner) {
            logger.warn("Jest runner not configured")
            return TestResult.builder()
                .framework('Jest')
                .errorOutput("Jest runner not configured")
                .build()
        }
        
        return jestRunner.runTests(options)
    }

    /**
     * Run only E2E tests (Appium).
     */
    TestResult runE2ETests(Map<String, Object> options = [:]) {
        ITestRunner appiumRunner = runners.find { it.frameworkName == 'Appium' }
        
        if (!appiumRunner) {
            logger.warn("Appium runner not configured")
            return TestResult.builder()
                .framework('Appium')
                .errorOutput("Appium runner not configured")
                .build()
        }
        
        return appiumRunner.runTests(options)
    }

    /**
     * Check if any test runner is configured.
     */
    boolean hasRunners() {
        return !runners.isEmpty()
    }

    /**
     * Get list of configured framework names.
     */
    List<String> getConfiguredFrameworks() {
        return runners.collect { it.frameworkName }
    }

    /**
     * Create from pipeline script.
     */
    static TestOrchestrator create(Object script) {
        return new TestOrchestrator(script)
    }
}
