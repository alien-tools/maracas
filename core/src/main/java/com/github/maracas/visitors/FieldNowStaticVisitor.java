package com.github.maracas.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtFieldReference;

/**
 * Broken uses of FIELD_NOW_STATIC are:
 * - Currently none
 * <p>
 * AFAIK, FIELD_NOW_STATIC is source-compatible (though japicmp says otherwise?)
 * We still need to implement broken uses for binary incompatibilities
 */
public class FieldNowStaticVisitor extends BreakingChangeVisitor {
	public FieldNowStaticVisitor(CtFieldReference<?> fRef) {
		super(JApiCompatibilityChange.FIELD_NOW_STATIC);
	}
}
