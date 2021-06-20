package org.swat.maracas.spoon.visitors;

import java.util.Optional;
import java.util.Set;

import org.swat.maracas.spoon.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

/**
 * Detections of CLASS_NOW_CHECKED_EXCEPTION are:
 *	- All expression throwing the now-checked-exception or one of its subtypes unless:
 *    - It is caught locally
 *    - The enclosing method declares the exception
 */
public class ClassNowCheckedExceptionVisitor extends BreakingChangeVisitor {
	private final CtTypeReference<?> clsRef;

	protected ClassNowCheckedExceptionVisitor(CtTypeReference<?> clsRef) {
		super(JApiCompatibilityChange.CLASS_NOW_CHECKED_EXCEPTION);
		this.clsRef = clsRef;
	}

	@Override
	public void visitCtThrow(CtThrow throwStatement) {
		CtTypeReference<? extends Throwable> thrownType = throwStatement.getThrownExpression().getType();
		if (thrownType.isSubtypeOf(clsRef)) {
			boolean isCaught = false;
			boolean isDeclared = false;
			
			CtTry enclosingTry = throwStatement.getParent(CtTry.class);
			if (enclosingTry != null) {
				Optional<CtCatch> excCatcher =
					enclosingTry.getCatchers()
					.stream()
					.filter(c -> thrownType.isSubtypeOf(c.getParameter().getType()))
					.findAny();
				
				if (excCatcher.isPresent())
					isCaught = true;
			}
			
			@SuppressWarnings("unchecked")
			Set<CtTypeReference<? extends Throwable>> thrownTypes =
				throwStatement.getParent(CtMethod.class)
				.getThrownTypes();
			
			Optional<CtTypeReference<? extends Throwable>> compatibleThrows =
				thrownTypes
				.stream()
				.filter(thrownType::isSubtypeOf)
				.findAny();
			
			if (compatibleThrows.isPresent())
				isDeclared = true;
			
			if (!isCaught && !isDeclared)
				detection(throwStatement, throwStatement.getThrownExpression().getType(), clsRef, APIUse.THROWS);
		}
	}
}
