package com.github.maracas.visitors;

import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.util.SpoonTypeHelpers;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Visitor in charge of gathering all method added to interface issues in
 * client code.
 * <p>
 * The visitor detects the following cases:
 * <ul>
 * <li> Concrete classes implementing the modified interface. Example:
 *      <pre>
 *      public class MyClass implements ModifiedInterface { }
 *      </pre>
 * </ul>
 */
public class MethodAddedToInterfaceVisitor extends BreakingChangeVisitor {
	/**
	 * Spoon reference to the interface where the new method has been added
	 */
	private final CtTypeReference<?> clsRef;

	/**
	 * Creates a MethodAddedToInterfaceVisitor instance.
	 *
	 * @param clsRef the interface where the new method has been added
	 */
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

			if (SpoonTypeHelpers.isSubtype(interfaces, clsRef))
				brokenUse(cls, cls, clsRef, APIUse.IMPLEMENTS);

			if (SpoonTypeHelpers.isSubtype(superCls, clsRef))
				brokenUse(cls, cls, clsRef, APIUse.EXTENDS);
		}
	}
}

