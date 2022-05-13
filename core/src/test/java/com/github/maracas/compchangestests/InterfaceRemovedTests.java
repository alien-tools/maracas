package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static com.github.maracas.brokenuse.APIUse.METHOD_INVOCATION;
import static com.github.maracas.brokenuse.APIUse.METHOD_OVERRIDE;
import static com.github.maracas.brokenuse.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.INTERFACE_REMOVED;
import static japicmp.model.JApiCompatibilityChange.SUPERCLASS_REMOVED;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class InterfaceRemovedTests extends CompChangesTest {

    @Test
    void testNoMore() {
        assertNumberBrokenUses(INTERFACE_REMOVED, 9);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testImplementsInter() {
        assertBrokenUse("InterfaceRemovedImp.java", 8, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testImplementsInterSubtype() {
        assertNoBrokenUse("InterfaceRemovedImp.java", 13, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testImplementsInterMulti() {
        assertBrokenUse("InterfaceRemovedImpMulti.java", 13, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testImplementsInterMultiSubtype1() {
        assertNoBrokenUse("InterfaceRemovedImpMulti.java", 8, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testImplementsInterMultiSubtype2() {
        assertNoBrokenUse("InterfaceRemovedImpMulti.java", 18, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Disabled("Issue with isSubtypeOf")
    @Test
    void testExtendsAbsClass() {
    	assertBrokenUse("InterfaceAddedExtAbs.java", 5, INTERFACE_REMOVED, METHOD_OVERRIDE);
    }

    @Test
    void testExtendsCastVar() {
        assertBrokenUse("InterfaceRemovedExt.java", 12, INTERFACE_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testExtendsSubtypeFieldAccessCTE() {
        assertBrokenUse("InterfaceRemovedExt.java", 16, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSubtypeStaticFieldAccessCTE() {
        assertNoBrokenUse("InterfaceRemovedExt.java", 20, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsDirectFieldAccessCTE() {
        assertBrokenUse("InterfaceRemovedExt.java", 24, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSubtypeFieldAccessList() {
        assertBrokenUse("InterfaceRemovedExt.java", 28, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSubtypeStaticFieldAccessList() {
        assertNoBrokenUse("InterfaceRemovedExt.java", 32, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsDirectFieldAccessList() {
        assertBrokenUse("InterfaceRemovedExt.java", 36, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testExtendsSupertypeInv() {
        assertNoBrokenUse("InterfaceRemovedExt.java", 41, INTERFACE_REMOVED, METHOD_INVOCATION);
    }

    @Test
    void testExtendsDirectInv() {
        assertBrokenUse("InterfaceRemovedExt.java", 48, INTERFACE_REMOVED, METHOD_INVOCATION);
    }

    @Test
    void testCastVar() {
        assertBrokenUse("InterfaceRemovedTD.java", 12, INTERFACE_REMOVED, TYPE_DEPENDENCY);
    }

    @Test
    void testSubtypeFieldAccessCTE() {
        assertBrokenUse("InterfaceRemovedTD.java", 16, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testSubtypeStaticFieldAccessCTE() {
        assertNoBrokenUse("InterfaceRemovedTD.java", 20, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testSubtypeFieldAccessList() {
        assertBrokenUse("InterfaceRemovedTD.java", 24, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
    void testSubtypeStaticFieldAccessList() {
        assertNoBrokenUse("InterfaceRemovedTD.java", 28, INTERFACE_REMOVED, FIELD_ACCESS);
    }

    @Test
	void testSubtypeInv() {
		assertNoBrokenUse("SuperclassRemovedTD.java", 33, SUPERCLASS_REMOVED, METHOD_INVOCATION);
	}
}
