package jenkins.pipeline.models

import groovy.transform.Immutable
import groovy.transform.ToString

/**
 * Immutable model representing application version information.
 * Follows semantic versioning conventions.
 */
@Immutable
@ToString(includeNames = true, includePackage = false)
class AppVersion implements Serializable, Comparable<AppVersion> {

    private static final long serialVersionUID = 1L

    /** Major version number */
    int major
    
    /** Minor version number */
    int minor
    
    /** Patch version number */
    int patch
    
    /** Build number / version code for Play Store */
    int buildNumber
    
    /** Optional pre-release label (e.g., 'beta', 'rc1') */
    String preRelease
    
    /** Optional build metadata (e.g., git commit hash) */
    String metadata

    /**
     * Get the semantic version string (e.g., "1.2.3").
     */
    String getVersionName() {
        String base = "${major}.${minor}.${patch}"
        if (preRelease) {
            base += "-${preRelease}"
        }
        if (metadata) {
            base += "+${metadata}"
        }
        return base
    }

    /**
     * Get the Android version code.
     * Uses buildNumber if set, otherwise calculates from major.minor.patch.
     */
    int getVersionCode() {
        if (buildNumber > 0) {
            return buildNumber
        }
        // Calculate: major * 10000 + minor * 100 + patch
        return (major * 10000) + (minor * 100) + patch
    }

    /**
     * Parse a version string to AppVersion.
     * Supports formats: "1.2.3", "1.2.3-beta", "1.2.3+abc123"
     * 
     * @param versionString Version string to parse
     * @param buildNumber Optional build number
     * @return AppVersion instance
     */
    static AppVersion parse(String versionString, int buildNumber = 0) {
        if (!versionString) {
            throw new IllegalArgumentException("Version string cannot be null or empty")
        }

        String version = versionString.trim()
        String preRelease = null
        String metadata = null

        // Extract metadata (after +)
        int metaIndex = version.indexOf('+')
        if (metaIndex >= 0) {
            metadata = version.substring(metaIndex + 1)
            version = version.substring(0, metaIndex)
        }

        // Extract pre-release (after -)
        int preIndex = version.indexOf('-')
        if (preIndex >= 0) {
            preRelease = version.substring(preIndex + 1)
            version = version.substring(0, preIndex)
        }

        // Remove 'v' prefix if present
        if (version.startsWith('v') || version.startsWith('V')) {
            version = version.substring(1)
        }

        // Parse version numbers
        String[] parts = version.split('\\.')
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid version format: ${versionString}")
        }

        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0

        return new AppVersion(
            major: major,
            minor: minor,
            patch: patch,
            buildNumber: buildNumber,
            preRelease: preRelease,
            metadata: metadata
        )
    }

    /**
     * Create a new version with incremented build number.
     */
    AppVersion incrementBuildNumber() {
        return new AppVersion(
            major: major,
            minor: minor,
            patch: patch,
            buildNumber: buildNumber + 1,
            preRelease: preRelease,
            metadata: metadata
        )
    }

    /**
     * Create a new version with incremented patch.
     */
    AppVersion incrementPatch() {
        return new AppVersion(
            major: major,
            minor: minor,
            patch: patch + 1,
            buildNumber: 0,
            preRelease: null,
            metadata: null
        )
    }

    /**
     * Create a new version with incremented minor.
     */
    AppVersion incrementMinor() {
        return new AppVersion(
            major: major,
            minor: minor + 1,
            patch: 0,
            buildNumber: 0,
            preRelease: null,
            metadata: null
        )
    }

    /**
     * Create a new version with incremented major.
     */
    AppVersion incrementMajor() {
        return new AppVersion(
            major: major + 1,
            minor: 0,
            patch: 0,
            buildNumber: 0,
            preRelease: null,
            metadata: null
        )
    }

    @Override
    int compareTo(AppVersion other) {
        if (other == null) return 1
        
        int result = this.major <=> other.major
        if (result != 0) return result
        
        result = this.minor <=> other.minor
        if (result != 0) return result
        
        result = this.patch <=> other.patch
        if (result != 0) return result
        
        return this.buildNumber <=> other.buildNumber
    }

    /**
     * Check if this version is greater than another.
     */
    boolean isNewerThan(AppVersion other) {
        return this.compareTo(other) > 0
    }
}
