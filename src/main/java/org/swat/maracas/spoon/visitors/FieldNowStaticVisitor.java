package org.swat.maracas.spoon.visitors;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.reference.CtFieldReference;

/**
 * Detections of FIELD_NOW_STATIC are:
 *	- Currently none
 *
 * AFAIK, FIELD_NOW_STATIC is source-compatible (though japicmp says otherwise?)
 * We still need to implement detections for binary incompatibilities
 */
public class FieldNowStaticVisitor extends BreakingChangeVisitor {
	private final CtFieldReference<?> fRef;

	protected FieldNowStaticVisitor(CtFieldReference<?> fRef) {
		super(JApiCompatibilityChange.FIELD_NOW_STATIC);
		this.fRef = fRef;
	}
}
