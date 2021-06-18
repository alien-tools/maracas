package org.swat.maracas.spoon.visitors;

import org.swat.maracas.spoon.Detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

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

		super.visitCtClass(ctClass);
	}

	@Override
	public <T> void visitCtNewClass(CtNewClass<T> newClass) {
		if (clsRef.equals(newClass.getType()))
			detection(newClass, newClass.getType(), clsRef, APIUse.EXTENDS);

		super.visitCtNewClass(newClass);
	}
}
