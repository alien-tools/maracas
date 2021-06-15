package org.swat.maracas.spoon;

import java.util.List;

import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtModuleReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeMemberWildcardImportReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.ReferenceTypeFilter;

public class SpoonHelper {
	public static List<CtReference> allReferencesToType(CtModel m, String fqn) {
		return Query.getReferences(m.getRootPackage(), new ReferenceTypeFilter<>(CtReference.class) {
			@Override
			public boolean matches(CtReference ref) {
				/**
				 * 3 ways to reference a type:
				 *   - Reference to the type itself
				 *   - Reference to one of its methods
				 *   - Reference to one of its fields
				 */
				if (ref instanceof CtTypeReference) {
					CtTypeReference<?> typeRef = (CtTypeReference<?>) ref;
					return fqn.equals(typeRef.getQualifiedName());
				} else if (ref instanceof CtExecutableReference) {
					CtExecutableReference<?> execRef = (CtExecutableReference<?>) ref;
					String container = execRef.getDeclaringType().getQualifiedName();
					return fqn.equals(container);
				} else  if (ref instanceof CtFieldReference) {
					CtFieldReference<?> fieldRef = (CtFieldReference<?>) ref;
					String container = fieldRef.getDeclaringType().getQualifiedName();
					return fqn.equals(container);
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

	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		while ((parent = parent.getParent()) != null) {
			if (parent.getPosition().getFile() != null && parent instanceof CtNamedElement)
				return parent;
		}
		return parent;
	}
}
