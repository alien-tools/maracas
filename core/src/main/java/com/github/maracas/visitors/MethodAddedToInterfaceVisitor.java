package com.github.maracas.visitors;

import java.util.HashSet;
import java.util.Set;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all method added to interface
 * issues in client code.
 * <p>
 * METHOD_ADDED_TO_INTERFACE detected cases:
 * <ul>
 * <li> Concrete classes directly implementing the interface.
 * <li> Concrete classes transitively implementing the interface.
 * </ul>
 */
public class MethodAddedToInterfaceVisitor extends BreakingChangeVisitor {

	private final CtTypeReference<?> clsRef;

	public MethodAddedToInterfaceVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.METHOD_ADDED_TO_INTERFACE);
		this.clsRef = clsRef;
	}

	@Override
	public <T> void visitCtClass(CtClass<T> cls) {
		if (!cls.isInterface() && !cls.isAbstract() && hasTransInterface(cls.getReference())) {
			detection(cls, cls, clsRef, APIUse.IMPLEMENTS);
		}
	}

	/**
	 * Verifies if a given type has the clsRef as a transitive
	 * interface.
	 * @param typeRef  given type
	 * @return         <code>true</code> if the type has the
	 *                 transitive interface given by clsRef;
	 *                 <code>false</code> otherwise.
	 */
	private boolean hasTransInterface(CtTypeReference<?> typeRef) {
		Set<CtTypeReference<?>> clsSupers = new HashSet<>(typeRef.getSuperInterfaces());
		CtTypeReference<?> clsSuper = typeRef.getSuperclass();
		if (clsSuper != null) {
			clsSupers.add(clsSuper);
		}

		boolean hasInter = false;
		for (CtTypeReference<?> sup : clsSupers) {
			if (clsRef.equals(sup)) {
				return true;
			}

			hasInter = hasInter || hasTransInterface(sup);
			if (hasInter) {
				break;
			}
		}

		return hasInter;
	}
}

