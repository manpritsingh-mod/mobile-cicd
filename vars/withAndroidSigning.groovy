#!/usr/bin/env groovy

/**
 * Execute code with Android signing credentials.
 * 
 * Usage:
 *   withAndroidSigning(keystoreCredentialsId: 'keystore', passwordCredentialsId: 'password') {
 *       androidBuild(buildType: 'release')
 *   }
 */
def call(Map params = [:], Closure body) {
    String keystoreCredId = params.keystoreCredentialsId ?: 'android-keystore'
    String passwordCredId = params.passwordCredentialsId ?: 'android-keystore-password'
    String keyAlias = params.keyAlias ?: 'release'
    
    withCredentials([
        file(credentialsId: keystoreCredId, variable: 'KEYSTORE_FILE'),
        string(credentialsId: passwordCredId, variable: 'KEYSTORE_PASSWORD')
    ]) {
        withEnv([
            "ANDROID_KEYSTORE_PATH=${env.KEYSTORE_FILE}",
            "ANDROID_KEYSTORE_PASSWORD=${env.KEYSTORE_PASSWORD}",
            "ANDROID_KEY_PASSWORD=${env.KEYSTORE_PASSWORD}",
            "ANDROID_KEY_ALIAS=${keyAlias}"
        ]) {
            echo "Signing configured with alias: ${keyAlias}"
            return body()
        }
    }
}
