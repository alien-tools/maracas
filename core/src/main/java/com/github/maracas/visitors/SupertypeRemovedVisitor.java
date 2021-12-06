package com.github.maracas.visitors;

import com.github.maracas.detection.APIUse;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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
	protected final Set<CtExecutableReference<?>> superMethods;

	protected SupertypeRemovedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> interfaces, JApiCompatibilityChange change) {
		super(change);
		this.clsRef = clsRef;
		this.superMethods = interfaces.stream()
			.map(i -> i.getDeclaredExecutables())
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		if (m.getDeclaringType().isSubtypeOf(clsRef)) {
			CtExecutableReference<?> superMeth = m.getReference().getOverridingExecutable();

			if (superMeth != null && superMethods.contains(superMeth))
				detection(m, superMeth, clsRef, APIUse.METHOD_OVERRIDE);
		}
	}
}
