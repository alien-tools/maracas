package org.swat.maracas.spoon;

import java.util.List;
import java.util.function.Predicate;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;

public class SpoonHelper {
	public static List<CtReference> allReferencesToType(CtModel m, CtTypeReference<?> typeRef, Predicate<CtReference> p) {
		return Query.getReferences(m.getRootPackage(),
				ref -> {
				/**
				 * 3 ways to reference a type:
				 *   - Reference to the type itself
				 *   - Reference to one of its methods
				 *   - Reference to one of its fields
				 * 
				 * FIXME: method polymorphism, etc.
				 */
				if (ref instanceof CtTypeReference) {
					return ref.equals(typeRef) && p.test(typeRef);
				} else if (ref instanceof CtExecutableReference) {
					return ((CtExecutableReference<?>) ref).getDeclaringType().equals(typeRef);
				} else  if (ref instanceof CtFieldReference) {
					return ((CtFieldReference<?>) ref).getDeclaringType().equals(typeRef);
				}

				return false;
		});
	}

	public static List<CtReference> allReferencesToType(CtModel m, CtTypeReference<?> typeRef) {
		return allReferencesToType(m, typeRef, ref -> true);
	}

	public static List<CtConstructorCall<?>> allInstantiationsOf(CtModel m, CtTypeReference<?> typeRef) {
		return Query.getElements(m.getRootPackage(),
			(CtConstructorCall<?> cons) -> typeRef.equals(cons.getType()));
	}

	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (parent.getPosition().getFile() != null)
				return parent;
		} while ((parent = parent.getParent()) != null);
		return parent;
	}

	public static boolean isConstructor(CtReference ref) {
		return ref instanceof CtExecutableReference && ((CtExecutableReference<?>) ref).isConstructor();
	}

	public static List<CtThrow> allExpressionsThrowing(CtModel m, CtTypeReference<?> typeRef) {
		return Query.getElements(m.getRootPackage(),
			(CtThrow thrw) -> thrw.getThrownExpression().getType().isSubtypeOf(typeRef));
	}

	public static List<CtClass<?>> allClassesExtending(CtModel m, CtTypeReference<?> typeRef) {
		return Query.getElements(m.getRootPackage(),
			(CtClass<?> cls) -> typeRef.equals(cls.getSuperclass()));
	}

	public static CtTypeReference<?> toTypeReference(CtModel m, String fqn) {
		return m.getRootPackage().getFactory().Type().createReference(fqn);
	}
}
