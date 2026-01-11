package jenkins.pipeline.interfaces

import jenkins.pipeline.models.BuildConfig

/**
 * Simple interface for mobile builders.
 */
interface IBuilder extends Serializable {

    /** Build the app */
    Map build(BuildConfig config)

    /** Clean build directory */
    boolean clean()

    /** Install npm/yarn dependencies */
    boolean installDependencies()

    /** Run lint checks */
    boolean runLint()

    /** Validate environment (returns list of errors) */
    List<String> validateEnvironment()
}
