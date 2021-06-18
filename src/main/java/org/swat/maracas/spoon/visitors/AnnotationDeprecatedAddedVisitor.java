package org.swat.maracas.spoon.visitors;

import java.lang.annotation.Annotation;

import org.swat.maracas.spoon.Detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

public class AnnotationDeprecatedAddedVisitor extends BreakingChangeVisitor {
	private final CtTypeReference<?> clsRef;

	protected AnnotationDeprecatedAddedVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED);
		this.clsRef = clsRef;
	}
	
	@Override
	public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
		if (clsRef.equals(annotation.getAnnotationType()))
			detection(annotation, annotation.getAnnotationType(), clsRef, APIUse.ANNOTATION);

		super.visitCtAnnotation(annotation);
	}

	@Override
	public <T> void visitCtClass(CtClass<T> ctClass) {
		if (clsRef.equals(ctClass.getSuperclass()))
			detection(ctClass, ctClass.getSuperclass(), clsRef, APIUse.EXTENDS);
		if (ctClass.getSuperInterfaces().contains(clsRef))
			detection(ctClass, clsRef, clsRef, APIUse.IMPLEMENTS);

		super.visitCtClass(ctClass);
	}

	@Override
	public <T> void visitCtNewClass(CtNewClass<T> newClass) {
		if (clsRef.equals(newClass.getType()))
			detection(newClass, newClass.getType(), clsRef, APIUse.EXTENDS);

		super.visitCtNewClass(newClass);
	}
	
	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
		// TODO Auto-generated method stub
		super.visitCtTypeReference(reference);
	}
}
