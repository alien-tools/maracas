package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.FIELD_ACCESS;
import static com.github.maracas.detection.APIUse.METHOD_INVOCATION;
import static com.github.maracas.detection.APIUse.METHOD_OVERRIDE;
import static com.github.maracas.detection.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.SUPERCLASS_REMOVED;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SuperclassRemovedTests extends CompChangesTest {

    @Test
    void testNoMore() {
        assertNumberDetections(SUPERCLASS_REMOVED, 13);
    }

    @Test
    void testExtendsAbsClass() {
        assertDetection("SuperclassRemovedExtAbs.java", 8, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testExtendsMultiAbsClass() {
        assertDetection("SuperclassRemovedImp.java", 8, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testExtendsMultiMultiAbsClass() {
        assertDetection("SuperclassRemovedImpMulti.java", 13, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testExtendsTransAbsClass() {
        assertDetection("SuperSuperclassRemovedExt.java", 13, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testExtendsCastVar() {
        assertDetection("SuperclassRemovedExt.java", 13, SUPERCLASS_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testExtendsSubtypeFieldAccessCTE() {
        assertDetection("SuperclassRemovedExt.java", 17, SUPERCLASS_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSubtypeFieldAccessList() {
        assertDetection("SuperclassRemovedExt.java", 29, SUPERCLASS_REMOVED, FIELD_ACCESS);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsDirectFieldAccessCTE() {
        assertDetection("SuperclassRemovedExt.java", 25, SUPERCLASS_REMOVED, FIELD_ACCESS);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsDirectFieldAccessList() {
        assertDetection("SuperclassRemovedExt.java", 37, SUPERCLASS_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSubtypeInv() {
        assertDetection("SuperclassRemovedExt.java", 41, SUPERCLASS_REMOVED, METHOD_INVOCATION);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsSupertypeInv() {
        assertNoDetection("SuperclassRemovedExt.java", 45, SUPERCLASS_REMOVED, METHOD_INVOCATION);
    }

    @Test
    void testExtendsDirectInv() {
        assertDetection("SuperclassRemovedExt.java", 49, SUPERCLASS_REMOVED, METHOD_INVOCATION);
    }

    @Test
    void testCastVar() {
        assertDetection("SuperclassRemovedTD.java", 14, SUPERCLASS_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testSubtypeFieldAccessCTE() {
        assertDetection("SuperclassRemovedTD.java", 18, SUPERCLASS_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testSubtypeFieldAccessList() {
        assertDetection("SuperclassRemovedTD.java", 26, SUPERCLASS_REMOVED, FIELD_ACCESS);
    }

    @Disabled("No implementation yet!")
    @Test
    void testSubtypeInv() {
        assertDetection("SuperclassRemovedTD.java", 34, SUPERCLASS_REMOVED, METHOD_INVOCATION);
    }
}
