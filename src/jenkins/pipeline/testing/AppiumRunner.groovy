package jenkins.pipeline.testing

import jenkins.pipeline.interfaces.ITestRunner
import jenkins.pipeline.logging.PipelineLogger

/**
 * Simple Appium E2E test runner.
 */
class AppiumRunner implements ITestRunner, Serializable {

    private static final long serialVersionUID = 1L

    private final Object script
    private final PipelineLogger logger
    private final String emulatorUrl
    private String appPath

    AppiumRunner(Object script, String emulatorUrl) {
        this.script = script
        this.logger = PipelineLogger.create(script)
        this.emulatorUrl = emulatorUrl
    }

    /**
     * Set app path (APK) for testing.
     */
    AppiumRunner withApp(String path) {
        this.appPath = path
        return this
    }

    @Override
    Map runTests() {
        logger.section("Running Appium E2E Tests")
        long startTime = System.currentTimeMillis()
        
        // Check emulator connection
        if (!checkEmulator()) {
            return [success: false, framework: 'Appium', error: 'Emulator not reachable']
        }
        
        // Set environment and run
        script.withEnv(["EMULATOR_URL=${emulatorUrl}", "APP_PATH=${appPath ?: ''}"]) {
            int exitCode = script.sh(
                script: 'npx wdio run wdio.conf.js',
                returnStatus: true
            )
            
            long duration = System.currentTimeMillis() - startTime
            
            if (exitCode == 0) {
                logger.success("E2E tests passed")
                return [success: true, framework: 'Appium', duration: duration]
            } else {
                logger.error("E2E tests failed")
                return [success: false, framework: 'Appium', duration: duration]
            }
        }
    }

    @Override
    boolean isAvailable() {
        return script.fileExists('wdio.conf.js')
    }

    @Override
    String getFrameworkName() {
        return 'Appium'
    }

    /**
     * Check if emulator is reachable.
     */
    private boolean checkEmulator() {
        if (!emulatorUrl) return false
        
        int exitCode = script.sh(
            script: "curl --connect-timeout 5 -s ${emulatorUrl}/status || true",
            returnStatus: true
        )
        return exitCode == 0
    }

    static AppiumRunner create(Object script, String emulatorUrl) {
        return new AppiumRunner(script, emulatorUrl)
    }
}
