package org.swat.maracas.spoon;

import java.util.List;
import java.util.stream.Collectors;

import org.swat.maracas.spoon.Detection.APIUse;

import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class SpoonHelper {
	public static List<Detection> allReferencesToType(CtModel m, String fqn) {
		return
			m.getElements(new TypeFilter<>(CtReference.class))
			.stream()
			.map(ref -> {
				if (ref instanceof CtTypeReference) {
					CtTypeReference<?> typeRef = (CtTypeReference<?>) ref;
					if (fqn.equals(typeRef.getQualifiedName())) {
						Detection d = new Detection();
						
						d.setElement(firstLocatableParent(typeRef));
						d.setReference(typeRef);
						d.setUsedApiElement(typeRef.getTypeDeclaration());
						d.setUse(APIUse.TYPE_DEPENDENCY);
						
//						if (typeRef.getPosition().getFile() != null)
							return d;
					}
				}

				if (ref instanceof CtExecutableReference<?>) {
					CtExecutableReference<?> execRef = (CtExecutableReference<?>) ref;
					String container = execRef.getDeclaringType().getQualifiedName();
					if (fqn.equals(container)) {
						Detection d = new Detection();
						d.setElement(firstLocatableParent(execRef));
						d.setReference(execRef);
						d.setUsedApiElement(execRef.getExecutableDeclaration());
						d.setUse(APIUse.METHOD_INVOCATION);
						
//						if (execRef.getPosition().getFile() != null)
							return d;
					}
				}
				
				if (ref instanceof CtFieldReference<?>) {
					CtFieldReference<?> fieldRef = (CtFieldReference<?>) ref;
					String container = fieldRef.getDeclaringType().getQualifiedName();
					if (fqn.equals(container)) {
						Detection d = new Detection();
						d.setElement(firstLocatableParent(fieldRef));
						d.setReference(fieldRef);
						d.setUsedApiElement(fieldRef.getFieldDeclaration());
						d.setUse(APIUse.FIELD_ACCESS);
						
//						if (fieldRef.getPosition().getFile() != null)
							return d;
					}
				}
				
				return null;
			})
			.filter(d -> d != null)
			.collect(Collectors.toList());
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
