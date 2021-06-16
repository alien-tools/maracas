package org.swat.maracas.spoon;

import java.util.List;
import java.util.function.Predicate;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtModuleReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeMemberWildcardImportReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.ReferenceTypeFilter;

public class SpoonHelper {
	public static List<CtReference> allReferencesToType(CtModel m, CtTypeReference<?> typeRef, Predicate<CtReference> p) {
		return Query.getReferences(m.getRootPackage(), new ReferenceTypeFilter<>(CtReference.class) {
			@Override
			public boolean matches(CtReference ref) {
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
//					return fqn.equals(typeRef.getQualifiedName()) && p.test(typeRef);
				} else if (ref instanceof CtExecutableReference) {
//					CtExecutableReference<?> execRef = (CtExecutableReference<?>) ref;
//					String container = execRef.getDeclaringType().getQualifiedName();
//					return fqn.equals(container) && p.test(execRef);
					return ((CtExecutableReference<?>) ref).getDeclaringType().equals(typeRef);
				} else  if (ref instanceof CtFieldReference) {
//					CtFieldReference<?> fieldRef = (CtFieldReference<?>) ref;
//					String container = fieldRef.getDeclaringType().getQualifiedName();
//					return fqn.equals(container) && p.test(fieldRef);
					return ((CtFieldReference<?>) ref).getDeclaringType().equals(typeRef);
				} else if (ref instanceof CtPackageReference) {
					return false;
				} else if (ref instanceof CtVariableReference) {
					return false;
				} else if (ref instanceof CtModuleReference) {
					return false;
				} else if (ref instanceof CtTypeMemberWildcardImportReference) {
					return false;
				} else {
					throw new UnsupportedOperationException("Unknown ref type " + ref.getClass());
				}
			}
		});
	}

	public static List<CtReference> allReferencesToType(CtModel m, CtTypeReference<?> typeRef) {
		return SpoonHelper.allReferencesToType(m, typeRef, ref -> true);
	}

	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		while ((parent = parent.getParent()) != null) {
			if (parent.getPosition().getFile() != null)
				return parent;
		}
		return parent;
	}

	public static boolean isConstructor(CtReference ref) {
		return ref instanceof CtExecutableReference && ((CtExecutableReference<?>) ref).isConstructor();
	}

	public static List<CtThrow> allThrown(CtModel m, CtTypeReference<?> typeRef) {
		return Query.getElements(m.getRootPackage(), new Filter<CtThrow>() {
			@Override
			public boolean matches(CtThrow thrw) {
				return thrw.getThrownExpression().getType().isSubtypeOf(typeRef);
			}
		});
	}

	public static CtTypeReference<?> toTypeReference(CtModel m, String fqn) {
		return m.getRootPackage().getFactory().Type().createReference(fqn);
	}
}
