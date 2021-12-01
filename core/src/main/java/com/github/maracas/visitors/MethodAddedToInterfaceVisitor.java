package com.github.maracas.visitors;

import java.util.Arrays;
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
		if (!cls.isAbstract()) {
			CtTypeReference<?> typeRef = cls.getReference();
			Set<CtTypeReference<?>> interfaces = new HashSet<>(typeRef.getSuperInterfaces());
			Set<CtTypeReference<?>> superCls = new HashSet<>(Arrays.asList(typeRef.getSuperclass()));

			if (isSubtype(interfaces)) {
				detection(cls, cls, clsRef, APIUse.IMPLEMENTS);
			}
			
			if (isSubtype(superCls)) {
				detection(cls, cls, clsRef, APIUse.EXTENDS);
			}
		}
	}
	
	/**
	 * Verifies if a set of type references are subtypes of the clsRef.
	 * @param superRefs set of type references
	 * @return          <code>true</code> if any of the types is a 
	 *                  subtype of the clsRef;
	 *                  <code>false</code> otherwise.
	 */
	private boolean isSubtype(Set<CtTypeReference<?>> superRefs) {	
		for (CtTypeReference<?> superRef : superRefs) {
			if (superRef == null) {
				return false;
			}
			if (superRef.equals(clsRef)) {
				return true;
			}
			
			if ((superRef.getTypeDeclaration().isAbstract() || superRef.isInterface()) 
				&& superRef.isSubtypeOf(clsRef)) {
				// FIXME: interfaces extending other interfaces are not considered 
				// by the isSubtypeOf() method
				Set<CtTypeReference<?>> clsSupers = new HashSet<>(superRef.getSuperInterfaces());
				clsSupers.add(superRef.getSuperclass());
				return isSubtype(clsSupers);
			} else {
				return false;
			}
		}
		
		return false;
	}
}

