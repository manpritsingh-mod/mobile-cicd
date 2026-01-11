#!/usr/bin/env groovy

import jenkins.pipeline.testing.JestRunner
import jenkins.pipeline.testing.AppiumRunner
import jenkins.pipeline.logging.PipelineLogger

/**
 * Run tests.
 * 
 * Usage:
 *   def result = androidTest(framework: 'jest')
 *   def result = androidTest(framework: 'appium', emulatorUrl: 'http://...')
 */
def call(Map params = [:]) {
    PipelineLogger logger = PipelineLogger.create(this)
    String framework = params.framework ?: 'jest'
    
    switch (framework.toLowerCase()) {
        case 'jest':
            JestRunner runner = new JestRunner(this, params.coverage != false)
            return runner.runTests()
            
        case 'appium':
            String emulatorUrl = params.emulatorUrl ?: env.EMULATOR_URL
            if (!emulatorUrl) {
                logger.error("emulatorUrl is required for Appium tests")
                return [success: false, error: 'No emulator URL']
            }
            
            AppiumRunner runner = new AppiumRunner(this, emulatorUrl)
            if (params.appPath) {
                runner.withApp(params.appPath)
            }
            return runner.runTests()
            
        default:
            logger.error("Unknown framework: ${framework}")
            return [success: false, error: "Unknown framework: ${framework}"]
    }
}
