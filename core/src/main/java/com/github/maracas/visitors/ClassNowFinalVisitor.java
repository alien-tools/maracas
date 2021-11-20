package com.github.maracas.visitors;

import java.util.Optional;

import com.github.maracas.detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of CLASS_NOW_FINAL are:
 *	- Classes (regular and anonymous) extending the now-final class
 *	- Methods @Override-ing a method of the now-final class
 *
 * Note that JApiCmp reports a CLASS_NOW_FINAL on types that go from {@code class}
 * to {@code enum}.
 */
public class ClassNowFinalVisitor extends BreakingChangeVisitor {
	private final CtTypeReference<?> clsRef;

	public ClassNowFinalVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.CLASS_NOW_FINAL);
		this.clsRef = clsRef;
	}

	@Override
	public <T> void visitCtClass(CtClass<T> ctClass) {
		if (clsRef.equals(ctClass.getSuperclass()))
			detection(ctClass, ctClass.getSuperclass(), clsRef, APIUse.EXTENDS);
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		if (m.hasAnnotation(java.lang.Override.class)) {
			Optional<CtMethod<?>> superMethod =
				m.getTopDefinitions()
					.stream()
					.filter(superM -> clsRef.equals(superM.getDeclaringType().getReference()))
					.findAny();

			superMethod.ifPresent(ctMethod -> detection(m, ctMethod, clsRef, APIUse.METHOD_OVERRIDE));
		}
	}

	@Override
	public <T> void visitCtNewClass(CtNewClass<T> newClass) {
		// Anonymous classes (CtNewClass) also go through (CtClass)
		// -> don't count twice
	}
}
