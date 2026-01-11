package jenkins.pipeline.interfaces

/**
 * Simple interface for test runners.
 */
interface ITestRunner extends Serializable {

    /** Run tests and return results map */
    Map runTests()

    /** Check if test framework is available */
    boolean isAvailable()

    /** Get framework name */
    String getFrameworkName()
}
