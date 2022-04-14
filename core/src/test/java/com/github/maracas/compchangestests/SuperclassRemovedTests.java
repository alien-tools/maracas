package com.github.maracas.compchangestests;

import static com.github.maracas.brokenuse.APIUse.FIELD_ACCESS;
import static com.github.maracas.brokenuse.APIUse.METHOD_INVOCATION;
import static com.github.maracas.brokenuse.APIUse.METHOD_OVERRIDE;
import static com.github.maracas.brokenuse.APIUse.TYPE_DEPENDENCY;
import static japicmp.model.JApiCompatibilityChange.SUPERCLASS_REMOVED;

import org.junit.jupiter.api.Test;

public class SuperclassRemovedTests extends CompChangesTest {

	@Test
	void testNoMore() {
		assertNumberBrokenUses(SUPERCLASS_REMOVED, 16);
	}

	@Test
	void testExtendsAbsClass() {
		assertBrokenUse("SuperclassRemovedExtAbs.java", 8, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testExtendsMultiAbsClass() {
		assertBrokenUse("SuperclassRemovedImp.java", 8, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testExtendsMultiAbsClassSubtype() {
		assertNoBrokenUse("SuperclassRemovedImp.java", 13, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testExtendsMultiMultiAbsClass() {
		assertBrokenUse("SuperclassRemovedImpMulti.java", 13, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testExtendsMultiMultiAbsClassSubtype1() {
		assertNoBrokenUse("SuperclassRemovedImpMulti.java", 8, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testExtendsMultiMultiAbsClassSubtype2() {
		assertNoBrokenUse("SuperclassRemovedImpMulti.java", 18, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testExtendsTransAbsClass() {
		assertBrokenUse("SuperSuperclassRemovedExt.java", 13, SUPERCLASS_REMOVED, METHOD_OVERRIDE);
	}

	@Test
	void testExtendsCastVar() {
		assertBrokenUse("SuperclassRemovedExt.java", 13, SUPERCLASS_REMOVED, TYPE_DEPENDENCY);
	}

	@Test
	void testExtendsSubtypeFieldAccessCTE() {
		assertBrokenUse("SuperclassRemovedExt.java", 17, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtendsStaticFieldAccessCTE() {
		assertNoBrokenUse("SuperclassRemovedExt.java", 21, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtendsDirectFieldAccessCTE() {
		assertBrokenUse("SuperclassRemovedExt.java", 25, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtendsSubtypeFieldAccessList() {
		assertBrokenUse("SuperclassRemovedExt.java", 29, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtendsStaticFieldAccessList() {
		assertNoBrokenUse("SuperclassRemovedExt.java", 33, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtendsDirectFieldAccessList() {
		assertBrokenUse("SuperclassRemovedExt.java", 37, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testExtendsSubtypeInv() {
		assertBrokenUse("SuperclassRemovedExt.java", 41, SUPERCLASS_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testExtendsSupertypeInv() {
		assertNoBrokenUse("SuperclassRemovedExt.java", 45, SUPERCLASS_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testExtendsDirectInv() {
		assertBrokenUse("SuperclassRemovedExt.java", 49, SUPERCLASS_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testCastVar() {
		assertBrokenUse("SuperclassRemovedTD.java", 14, SUPERCLASS_REMOVED, TYPE_DEPENDENCY);
	}

	@Test
	void testSubtypeFieldAccessCTE() {
		assertBrokenUse("SuperclassRemovedTD.java", 18, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testSubtypeStaticFieldAccessCTE() {
		assertNoBrokenUse("SuperclassRemovedTD.java", 22, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testSubtypeFieldAccessList() {
		assertBrokenUse("SuperclassRemovedTD.java", 26, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testSubtypeStaticFieldAccessList() {
		assertNoBrokenUse("SuperclassRemovedTD.java", 30, SUPERCLASS_REMOVED, FIELD_ACCESS);
	}

	@Test
	void testSubtypeInv() {
		assertBrokenUse("SuperclassRemovedTD.java", 34, SUPERCLASS_REMOVED, METHOD_INVOCATION);
	}

	@Test
	void testSubtypeStaticInv() {
		assertNoBrokenUse("SuperclassRemovedTD.java", 38, SUPERCLASS_REMOVED, METHOD_INVOCATION);
	}
}
