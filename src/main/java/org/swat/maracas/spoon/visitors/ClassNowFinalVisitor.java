package org.swat.maracas.spoon.visitors;

import java.util.Optional;

import org.swat.maracas.spoon.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of CLASS_NOW_FINAL are:
 *	- Classes (regular and anonymous) extending the now-final class
 *	- Methods @Override-ing a method of the now-final class
 */
public class ClassNowFinalVisitor extends BreakingChangeVisitor {
	private final CtTypeReference<?> clsRef;

	protected ClassNowFinalVisitor(CtTypeReference<?> clsRef) {
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

			if (superMethod.isPresent())
				detection(m, superMethod.get(), clsRef, APIUse.METHOD_OVERRIDE);
		}
	}

	@Override
	public <T> void visitCtNewClass(CtNewClass<T> newClass) {
		// Anonymous classes (CtNewClass) also go through (CtClass)
		// -> don't count twice

		// if (clsRef.equals(newClass.getType()))
		//	detection(newClass, newClass.getType(), clsRef, APIUse.EXTENDS);
	}
}
