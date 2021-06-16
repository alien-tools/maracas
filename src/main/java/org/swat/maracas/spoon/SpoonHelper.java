package org.swat.maracas.spoon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.swat.maracas.spoon.Detection.APIUse;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

public class SpoonHelper {
	public static List<Detection> allReferencesToType(CtModel m, CtTypeReference<?> typeRef) {
		List<Detection> ds = new ArrayList<>();
		
		for (CtReference ref : Query.getElements(m.getRootPackage(), new TypeFilter<>(CtReference.class))) {
			if (ref instanceof CtTypeReference) {
				if (typeRef.equals(ref))
					if (ref.getParent() instanceof CtType) {
						CtType<?> typ = (CtType<?>) ref.getParent();
						if (ref.equals(typ.getSuperclass()))
							ds.add(toDetection(typ, typeRef.getTypeDeclaration(), APIUse.EXTENDS));
						else if (typ.getSuperInterfaces().contains(ref))
							ds.add(toDetection(typ, typeRef.getTypeDeclaration(), APIUse.IMPLEMENTS));
					} else
						ds.add(toDetection(ref, typeRef.getTypeDeclaration(), APIUse.TYPE_DEPENDENCY));
			} else if (ref instanceof CtExecutableReference) {
				CtExecutableReference<?> execRef = (CtExecutableReference<?>) ref;
				if (typeRef.equals(execRef.getDeclaringType()))
					ds.add(toDetection(execRef, execRef.getExecutableDeclaration(), APIUse.METHOD_INVOCATION));
			} else if (ref instanceof CtFieldReference) {
				CtFieldReference<?> fieldRef = (CtFieldReference<?>) ref;
				if (typeRef.equals(fieldRef.getDeclaringType()))
					ds.add(toDetection(fieldRef, fieldRef.getFieldDeclaration(), APIUse.FIELD_ACCESS));
			}
		}
	
		return ds;
	}

	public static Detection toDetection(CtElement element, CtElement used, APIUse use) {
		Detection d = new Detection();
		d.setElement(firstLocatableParent(element));
		d.setUsedApiElement(used);
		d.setUse(use);
		return d;
	}

	public static CtElement firstLocatableParent(CtElement element) {
		CtElement parent = element;
		do {
			if (parent.getPosition().getFile() != null)
				return parent;
		} while ((parent = parent.getParent()) != null);
		return parent;
	}

	public static List<Detection> allInstantiationsOf(CtModel m, CtTypeReference<?> typeRef) {
		return
			Query.getElements(m.getRootPackage(), new TypeFilter<>(CtConstructorCall.class))
				.stream()
				.filter(cons -> typeRef.equals(cons.getType()))
				.map(cons -> toDetection(cons, cons.getType(), APIUse.METHOD_INVOCATION))
				.collect(Collectors.toList());
	}

	public static List<Detection> allExpressionsThrowing(CtModel m, CtTypeReference<?> typeRef) {
		return
			Query.getElements(m.getRootPackage(), new TypeFilter<>(CtThrow.class))
				.stream()
				.filter(thrw -> thrw.getThrownExpression().getType().isSubtypeOf(typeRef))
				.map(thrw -> toDetection(thrw, thrw.getThrownExpression().getType(), APIUse.TYPE_DEPENDENCY))
				.collect(Collectors.toList());
	}

	public static List<Detection> allExtensionsOf(CtModel m, CtTypeReference<?> typeRef) {
		return
			Query.getElements(m.getRootPackage(), new TypeFilter<>(CtClass.class))
				.stream()
				.filter(cls -> typeRef.equals(cls.getSuperclass()))
				.map(cls -> toDetection(cls, cls.getSuperclass(), APIUse.EXTENDS))
				.collect(Collectors.toList());
	}

	public static List<Detection> allImplementationsOf(CtModel m, CtTypeReference<?> typeRef) {
		return
				Query.getElements(m.getRootPackage(), new TypeFilter<>(CtType.class))
					.stream()
					.filter(cls -> cls.getSuperInterfaces().contains(typeRef))
					.map(cls -> toDetection(cls, typeRef, APIUse.IMPLEMENTS))
					.collect(Collectors.toList());
	}

	public static List<Detection> allAnnotationsOfType(CtModel m, CtTypeReference<?> typeRef) {
		return
				Query.getElements(m.getRootPackage(), new TypeFilter<>(CtAnnotation.class))
					.stream()
					.filter(ann -> typeRef.equals(ann.getAnnotationType()))
					.map(ann -> toDetection(ann, ann.getAnnotationType(), APIUse.IMPLEMENTS))
					.collect(Collectors.toList());
	}

	public static CtTypeReference<?> toTypeReference(CtModel m, String fqn) {
		return m.getRootPackage().getFactory().Type().createReference(fqn);
	}
}
