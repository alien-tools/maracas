package com.github.maracas.compchangestests;

import static com.github.maracas.brokenUse.APIUse.FIELD_ACCESS;
import static com.github.maracas.brokenUse.APIUse.METHOD_INVOCATION;
import static com.github.maracas.brokenUse.APIUse.METHOD_OVERRIDE;
import static com.github.maracas.brokenUse.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.INTERFACE_REMOVED;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class InterfaceRemovedTests extends CompChangesTest {

    @Test
    void testNoMore() {
        assertNumberBrokenUses(INTERFACE_REMOVED, 8);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testImplementsInter() {
        assertBrokenUse("InterfaceRemovedImp.java", 8, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testImplementsInterMulti() {
        assertBrokenUse("InterfaceRemovedImpMulti.java", 13, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testExtendsAbsClass() {
    	assertBrokenUse("InterfaceAddedExtAbs.java", 5, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsSupertypeInv() {
        assertNoBrokenUse("InterfaceRemovedExt.java", 45, INTERFACE_REMOVED, METHOD_INVOCATION);
    }

    @Test
    void testExtendsDirectInv() {
        assertBrokenUse("InterfaceRemovedExt.java", 48, INTERFACE_REMOVED, METHOD_INVOCATION);
    }

    @Disabled("Shall we report this case or not?")
    @Test
    void testExtendsCastVar() {
        assertNoBrokenUse("InterfaceRemovedExt.java", 12, INTERFACE_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testExtendsSubtypeFieldAccessCTE() {
        assertBrokenUse("InterfaceRemovedExt.java", 16, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSubtypeFieldAccessList() {
        assertBrokenUse("InterfaceRemovedExt.java", 28, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsDirectFieldAccessCTE() {
        assertBrokenUse("InterfaceRemovedExt.java", 24, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Disabled("No implementation yet!")
    @Test
    void testExtendsDirectFieldAccessList() {
        assertBrokenUse("InterfaceRemovedExt.java", 36, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Disabled("Shall we report this case or not?")
    @Test
    void testCastVar() {
        assertBrokenUse("InterfaceRemovedTD.java", 12, INTERFACE_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testSubtypeFieldAccessCTE() {
        assertBrokenUse("InterfaceRemovedTD.java", 16, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testSubtypeFieldAccessList() {
        assertBrokenUse("InterfaceRemovedTD.java", 24, INTERFACE_REMOVED, FIELD_ACCESS);
    }
}
