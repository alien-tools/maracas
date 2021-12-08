package com.github.maracas.compchangestests;

import static com.github.maracas.detection.APIUse.FIELD_ACCESS;
import static com.github.maracas.detection.APIUse.METHOD_INVOCATION;
import static com.github.maracas.detection.APIUse.METHOD_OVERRIDE;
import static com.github.maracas.detection.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.INTERFACE_REMOVED;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class InterfaceRemovedTests extends CompChangesTest {

    @Test
    void testNoMore() {
        assertNumberDetections(INTERFACE_REMOVED, 8);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testImplementsInter() {
        assertDetection("InterfaceRemovedImp.java", 8, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testImplementsInterMulti() {
        assertDetection("InterfaceRemovedImpMulti.java", 13, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testExtendsAbsClass() {
    	assertDetection("InterfaceAddedExtAbs.java", 5, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsSupertypeInv() {
        assertNoDetection("InterfaceRemovedExt.java", 45, INTERFACE_REMOVED, METHOD_INVOCATION);
    }

    @Test
    void testExtendsDirectInv() {
        assertDetection("InterfaceRemovedExt.java", 48, INTERFACE_REMOVED, METHOD_INVOCATION);
    }

    @Disabled("Shall we report this case or not?")
    @Test
    void testExtendsCastVar() {
        assertNoDetection("InterfaceRemovedExt.java", 12, INTERFACE_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testExtendsSubtypeFieldAccessCTE() {
        assertDetection("InterfaceRemovedExt.java", 16, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSubtypeFieldAccessList() {
        assertDetection("InterfaceRemovedExt.java", 28, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsDirectFieldAccessCTE() {
        assertDetection("InterfaceRemovedExt.java", 24, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsDirectFieldAccessList() {
        assertDetection("InterfaceRemovedExt.java", 36, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Disabled("Shall we report this case or not?")
    @Test
    void testCastVar() {
        assertDetection("InterfaceRemovedTD.java", 12, INTERFACE_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testSubtypeFieldAccessCTE() {
        assertDetection("InterfaceRemovedTD.java", 16, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testSubtypeFieldAccessList() {
        assertDetection("InterfaceRemovedTD.java", 24, INTERFACE_REMOVED, FIELD_ACCESS);
    }
}
