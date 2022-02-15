package com.github.maracas.build;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Upgrade of a Maven artifact. Includes old and new versions of some of the
 * artifact coordinates.
 */
public record MavenArtifactUpgrade(
    String oldGroupId,
    String newGroupId,
    String oldArtifactId,
    String newArtifactId,
    String oldVersion,
    String newVersion) {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MavenArtifactUpgrade.class);

    /**
     * Creates a MavenArtifactUpgrade instance and validates that there is at
     * least one defined field and that there is a pair for each defined artifact
     * attribute (e.g., oldArtifactId and newArtifactId have been defined).
     *
     * @param oldGroupId    old groupId of the artifact
     * @param newGroupId    new groupId of the artifact
     * @param oldArtifactId old artifactId of the artifact
     * @param newArtifactId new artifactId of the artifact
     * @param oldVersion    old version of the artifact
     * @param newVersion    new version of the artifact
     */
    public MavenArtifactUpgrade(String oldGroupId, String newGroupId,
        String oldArtifactId, String newArtifactId, String oldVersion, String newVersion) {
        validateUpgrade();
        validateUpgradeValues("groupId", oldGroupId, newGroupId);
        validateUpgradeValues("artifactId", oldArtifactId, newArtifactId);
        validateUpgradeValues("version", oldVersion, newVersion);

        this.oldGroupId = oldGroupId;
        this.newGroupId = newGroupId;
        this.oldArtifactId = oldArtifactId;
        this.newArtifactId = newArtifactId;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    /**
     * Validates that the old and new values of a artifact attribute are both
     * defined if at least one of the two has been specified by the user.
     *
     * @param valId    artifact attribute (i.e., groupId, artifactId, and version)
     * @param oldValue old value of the attribute
     * @param newValue new attribute of the value
     */
    private void validateUpgradeValues(String valId, String oldValue, String newValue) {
        if (oldValue != null || newValue != null)
            assert oldValue != null && newValue != null:
                String.format("Missing %s upgrade value", valId);
    }

    /**
     * Validates that at least one of the fields of the type has been defined.
     */
    private void validateUpgrade() {
        assert oldGroupId != null
            || newGroupId != null
            || oldArtifactId != null
            || newArtifactId != null
            || oldVersion != null
            || newVersion != null
            : "All upgrade values are null";
    }
}
