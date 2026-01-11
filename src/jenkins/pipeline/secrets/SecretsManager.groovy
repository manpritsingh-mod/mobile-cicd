package jenkins.pipeline.secrets

/**
 * Manager for handling Jenkins credentials securely.
 * Provides type-safe access to different credential types.
 */
class SecretsManager implements Serializable {

    private static final long serialVersionUID = 1L

    private final Object script

    SecretsManager(Object script) {
        this.script = script
    }

    /**
     * Execute closure with string credential.
     * @param credentialsId Jenkins credentials ID
     * @param varName Environment variable name
     * @param closure Code to execute with credential
     */
    def withStringCredential(String credentialsId, String varName, Closure closure) {
        script.withCredentials([
            script.string(credentialsId: credentialsId, variable: varName)
        ]) {
            closure()
        }
    }

    /**
     * Execute closure with file credential.
     * @param credentialsId Jenkins credentials ID
     * @param varName Environment variable name
     * @param closure Code to execute with file path
     */
    def withFileCredential(String credentialsId, String varName, Closure closure) {
        script.withCredentials([
            script.file(credentialsId: credentialsId, variable: varName)
        ]) {
            closure()
        }
    }

    /**
     * Execute closure with username/password credential.
     * @param credentialsId Jenkins credentials ID
     * @param usernameVar Username variable name
     * @param passwordVar Password variable name
     * @param closure Code to execute
     */
    def withUsernamePassword(String credentialsId, String usernameVar, 
                              String passwordVar, Closure closure) {
        script.withCredentials([
            script.usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: usernameVar,
                passwordVariable: passwordVar
            )
        ]) {
            closure()
        }
    }

    /**
     * Execute closure with SSH key credential.
     * @param credentialsId Jenkins credentials ID
     * @param keyVar Key file variable name
     * @param passphraseVar Passphrase variable name
     * @param usernameVar Username variable name
     * @param closure Code to execute
     */
    def withSshKey(String credentialsId, String keyVar, String passphraseVar = null,
                   String usernameVar = null, Closure closure) {
        List bindings = []
        
        Map sshBinding = [
            credentialsId: credentialsId,
            keyFileVariable: keyVar
        ]
        
        if (passphraseVar) {
            sshBinding['passphraseVariable'] = passphraseVar
        }
        if (usernameVar) {
            sshBinding['usernameVariable'] = usernameVar
        }
        
        bindings << script.sshUserPrivateKey(sshBinding)
        
        script.withCredentials(bindings) {
            closure()
        }
    }

    /**
     * Execute closure with Android signing credentials.
     * @param keystoreCredId Keystore file credentials ID
     * @param passwordCredId Password credentials ID
     * @param keyAlias Key alias
     * @param closure Code to execute with signing environment set up
     */
    def withAndroidSigning(String keystoreCredId, String passwordCredId, 
                           String keyAlias, Closure closure) {
        script.withCredentials([
            script.file(credentialsId: keystoreCredId, variable: 'KEYSTORE_FILE'),
            script.string(credentialsId: passwordCredId, variable: 'KEYSTORE_PASSWORD')
        ]) {
            script.withEnv([
                "ANDROID_KEY_ALIAS=${keyAlias}",
                "ANDROID_KEYSTORE_PATH=${script.env.KEYSTORE_FILE}",
                "ANDROID_KEYSTORE_PASSWORD=${script.env.KEYSTORE_PASSWORD}",
                "ANDROID_KEY_PASSWORD=${script.env.KEYSTORE_PASSWORD}"
            ]) {
                closure()
            }
        }
    }

    /**
     * Execute closure with Play Store credentials.
     * @param jsonKeyCredId Service account JSON key credentials ID
     * @param closure Code to execute
     */
    def withPlayStoreCredentials(String jsonKeyCredId, Closure closure) {
        script.withCredentials([
            script.file(credentialsId: jsonKeyCredId, variable: 'GOOGLE_PLAY_JSON_KEY')
        ]) {
            closure()
        }
    }

    /**
     * Execute closure with Slack webhook.
     * @param webhookCredId Webhook URL credentials ID
     * @param closure Code to execute
     */
    def withSlackWebhook(String webhookCredId, Closure closure) {
        script.withCredentials([
            script.string(credentialsId: webhookCredId, variable: 'SLACK_WEBHOOK_URL')
        ]) {
            closure()
        }
    }

    /**
     * Execute closure with multiple credentials.
     * @param credentials List of credential maps
     * @param closure Code to execute
     */
    def withMultipleCredentials(List<Map<String, Object>> credentials, Closure closure) {
        List bindings = credentials.collect { cred ->
            switch (cred.type) {
                case 'string':
                    return script.string(credentialsId: cred.id, variable: cred.variable)
                case 'file':
                    return script.file(credentialsId: cred.id, variable: cred.variable)
                case 'usernamePassword':
                    return script.usernamePassword(
                        credentialsId: cred.id,
                        usernameVariable: cred.usernameVar,
                        passwordVariable: cred.passwordVar
                    )
                default:
                    throw new IllegalArgumentException("Unknown credential type: ${cred.type}")
            }
        }
        
        script.withCredentials(bindings) {
            closure()
        }
    }

    /**
     * Mask a secret value in logs.
     * @param value Value to mask
     */
    void maskSecret(String value) {
        if (value) {
            script.wrap([$class: 'MaskPasswordsBuildWrapper', 
                        varPasswordPairs: [[password: value]]]) {
                // This wraps the entire build
            }
        }
    }

    /**
     * Create from pipeline script.
     */
    static SecretsManager create(Object script) {
        return new SecretsManager(script)
    }
}
