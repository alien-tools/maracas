package org.swat.maracas.spoon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.swat.maracas.spoon.Detection.APIUse;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtNewClass;
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
				if (typeRef.equals(ref)) {
					if (ref.getParent() instanceof CtType typ) {
						if (ref.equals(typ.getSuperclass())) {
							ds.add(toDetection(typ, typeRef.getTypeDeclaration(), APIUse.EXTENDS));
							continue;
						} else if (typ.getSuperInterfaces().contains(ref)) {
							ds.add(toDetection(typ, typeRef.getTypeDeclaration(), APIUse.IMPLEMENTS));
							continue;
						}
					}
					ds.add(toDetection(ref, typeRef.getTypeDeclaration(), APIUse.TYPE_DEPENDENCY));
				}
			} else if (ref instanceof CtExecutableReference execRef) {
				if (typeRef.equals(execRef.getDeclaringType()))
					ds.add(toDetection(execRef, execRef.getExecutableDeclaration(), APIUse.METHOD_INVOCATION));
			} else if (ref instanceof CtFieldReference fieldRef) {
				if (typeRef.equals(fieldRef.getDeclaringType()))
					ds.add(toDetection(fieldRef, fieldRef.getFieldDeclaration(), APIUse.FIELD_ACCESS));
			}
		}

		System.out.println("ds="+ds.size());

		return ds;
	}

	public static List<Detection> allReferencesToMethod(CtModel m, CtExecutableReference<?> mthRef) {
		return
			Query.getElements(m.getRootPackage(), new TypeFilter<>(CtExecutableReference.class))
				.stream()
				.filter(ref -> ref.equals(mthRef))
				.map(ref -> toDetection(ref, ref.getExecutableDeclaration(), APIUse.METHOD_INVOCATION))
				.collect(Collectors.toList());
	}

	public static Detection toDetection(CtElement element, CtElement used, APIUse use) {
		Detection d = new Detection();
		//d.setElement(firstLocatableParent(element));
		d.setElement(element);
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
			Query.getElements(m.getRootPackage(), (CtConstructorCall<?> cons) -> typeRef.equals(cons.getType()))
				.stream()
				.map(cons -> toDetection(cons, cons.getType(), APIUse.METHOD_INVOCATION))
				.collect(Collectors.toList());
	}

	public static List<Detection> allExpressionsThrowing(CtModel m, CtTypeReference<?> typeRef) {
		return
			Query.getElements(m.getRootPackage(), (CtThrow thrw) -> thrw.getThrownExpression().getType().isSubtypeOf(typeRef))
				.stream()
				.map(thrw -> toDetection(thrw, thrw.getThrownExpression().getType(), APIUse.TYPE_DEPENDENCY))
				.collect(Collectors.toList());
	}

	public static List<Detection> allAnonymousClassesOf(CtModel m, CtTypeReference<?> typeRef) {
		return
				Query.getElements(m.getRootPackage(), (CtNewClass<?> cls) -> typeRef.equals(cls.getType()))
					.stream()
					.map(cls -> toDetection(cls, cls.getAnonymousClass(), APIUse.EXTENDS))
					.collect(Collectors.toList());
	}

	public static List<Detection> allClassesExtending(CtModel m, CtTypeReference<?> typeRef) {
		return
			Query.getElements(m.getRootPackage(), (CtClass<?> cls) -> typeRef.equals(cls.getSuperclass()))
				.stream()
				.map(cls -> toDetection(cls, cls.getSuperclass(), APIUse.EXTENDS))
				.collect(Collectors.toList());
	}

	public static List<Detection> allClassesImplementing(CtModel m, CtTypeReference<?> typeRef) {
		return
				Query.getElements(m.getRootPackage(), (CtClass<?> cls) -> cls.getSuperInterfaces().contains(typeRef))
					.stream()
					.map(cls -> toDetection(cls, typeRef, APIUse.IMPLEMENTS))
					.collect(Collectors.toList());
	}

	public static List<Detection> allAnnotationsOfType(CtModel m, CtTypeReference<?> typeRef) {
		return
				Query.getElements(m.getRootPackage(), (CtAnnotation<?> ann) -> typeRef.equals(ann.getAnnotationType()))
					.stream()
					.map(ann -> toDetection(ann, ann.getAnnotationType(), APIUse.IMPLEMENTS))
					.collect(Collectors.toList());
	}

	public static CtTypeReference<?> toTypeReference(CtModel m, String fqn) {
		return m.getRootPackage().getFactory().Type().createReference(fqn);
	}
}
