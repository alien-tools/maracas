package com.github.maracas.visitors;

import java.util.HashSet;
import java.util.Set;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all supertype removed issues in client code.
 * 
 * <p>INTERFACE_REMOVED and SUPERCLASS_REMOVED detected cases:
 * <ul>
 * <li> Methods overriding methods declared within the supertype.
 *      Example: <pre>@Override<b>public void m() { return; }</pre>
 */
public class SupertypeRemovedVisitor extends BreakingChangeVisitor {

	protected final CtTypeReference<?> clsRef;
	protected final Set<CtTypeReference<?>> supersRef;
	protected final Set<CtExecutableReference<?>> methsRef;
	
	protected SupertypeRemovedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> supersRef, JApiCompatibilityChange change) {
		super(change);
		this.clsRef = clsRef;
		this.supersRef = supersRef;
		this.methsRef = new HashSet<CtExecutableReference<?>>();
		
		for (CtTypeReference<?> superRef : supersRef) {
			if (superRef != null) {
				this.methsRef.addAll(superRef.getDeclaredExecutables());
			}
		}
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		CtExecutableReference<?> superMeth = m.getReference().getOverridingExecutable();
		CtTypeReference<?>superCls = m.getDeclaringType().getReference();
		
		if (superMeth != null && superCls.isSubtypeOf(clsRef) 
				&& methsRef.contains(superMeth)) {
			detection(m, superMeth, clsRef, APIUse.METHOD_OVERRIDE);
		}
	}
}
