package jenkins.pipeline.exceptions

import jenkins.pipeline.enums.PlayStoreTrack

/**
 * Exception thrown when deployment/distribution fails.
 */
class DeployException extends PipelineException {

    private static final long serialVersionUID = 1L

    /** Target track for deployment */
    final PlayStoreTrack track
    
    /** Artifact path that failed to deploy */
    final String artifactPath

    DeployException(String message) {
        super(message, 'Deploy')
        this.track = null
        this.artifactPath = null
    }

    DeployException(String message, PlayStoreTrack track) {
        super(message, null, 'Deploy',
              generateRemediation(message),
              [track: track?.trackName])
        this.track = track
        this.artifactPath = null
    }

    DeployException(String message, PlayStoreTrack track, String artifactPath) {
        super(message, null, 'Deploy',
              generateRemediation(message),
              [track: track?.trackName, artifact: artifactPath])
        this.track = track
        this.artifactPath = artifactPath
    }

    DeployException(String message, Throwable cause) {
        super(message, cause, 'Deploy')
        this.track = null
        this.artifactPath = null
    }

    private static List<String> generateRemediation(String message) {
        String lowerMessage = message?.toLowerCase() ?: ''
        
        if (lowerMessage.contains('authentication') || lowerMessage.contains('credential')) {
            return [
                'Verify Play Store service account JSON key is valid',
                'Check Jenkins credentials are properly configured',
                'Ensure service account has correct API permissions'
            ]
        }
        
        if (lowerMessage.contains('version') || lowerMessage.contains('versioncode')) {
            return [
                'Version code must be higher than current version in Play Store',
                'Check app/build.gradle for version settings',
                'Consider using auto-incrementing version codes'
            ]
        }
        
        if (lowerMessage.contains('signature') || lowerMessage.contains('signing')) {
            return [
                'Verify upload keystore matches the one registered with Play Store',
                'Check keystore password and alias are correct',
                'Ensure APK/AAB is properly signed'
            ]
        }
        
        return [
            'Review Fastlane output for detailed error',
            'Check Play Console for any pending issues',
            'Verify app bundle is properly formatted'
        ]
    }
}
