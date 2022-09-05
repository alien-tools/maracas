package com.github.maracas.visitors;

import com.github.maracas.MaracasOptions;
import com.github.maracas.brokenuse.BrokenUse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.CtScanner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Traverses the AST once and delegates to the registered
 * visitors when visiting each node.
 */
public class CombinedVisitor extends CtScanner {
	private final Collection<BreakingChangeVisitor> visitors;
	private final MaracasOptions options;

	private static final Logger logger = LogManager.getLogger(CombinedVisitor.class);

	public CombinedVisitor(Collection<BreakingChangeVisitor> visitors, MaracasOptions options) {
		this.visitors = visitors;
		this.options = options;
	}

	public Set<BrokenUse> getBrokenUses() {
		return
			visitors.stream()
				.map(BreakingChangeVisitor::getBrokenUses)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	@Override
	public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
		visitors.forEach(v -> v.visitCtAnnotation(annotation));
		super.visitCtAnnotation(annotation);
	}

	@Override
	public <T> void visitCtCodeSnippetExpression(CtCodeSnippetExpression<T> expression) {
		visitors.forEach(v -> v.visitCtCodeSnippetExpression(expression));
		super.visitCtCodeSnippetExpression(expression);
	}

	@Override
	public void visitCtCodeSnippetStatement(CtCodeSnippetStatement statement) {
		visitors.forEach(v -> v.visitCtCodeSnippetStatement(statement));
		super.visitCtCodeSnippetStatement(statement);
	}

	@Override
	public <A extends Annotation> void visitCtAnnotationType(CtAnnotationType<A> annotationType) {
		visitors.forEach(v -> v.visitCtAnnotationType(annotationType));
		super.visitCtAnnotationType(annotationType);
	}

	@Override
	public void visitCtAnonymousExecutable(CtAnonymousExecutable anonymousExec) {
		visitors.forEach(v -> v.visitCtAnonymousExecutable(anonymousExec));
		super.visitCtAnonymousExecutable(anonymousExec);
	}

	@Override
	public <T> void visitCtArrayRead(CtArrayRead<T> arrayRead) {
		visitors.forEach(v -> v.visitCtArrayRead(arrayRead));
		super.visitCtArrayRead(arrayRead);
	}

	@Override
	public <T> void visitCtArrayWrite(CtArrayWrite<T> arrayWrite) {
		visitors.forEach(v -> v.visitCtArrayWrite(arrayWrite));
		super.visitCtArrayWrite(arrayWrite);
	}

	@Override
	public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> reference) {
		visitors.forEach(v -> v.visitCtArrayTypeReference(reference));
		super.visitCtArrayTypeReference(reference);
	}

	@Override
	public <T> void visitCtAssert(CtAssert<T> asserted) {
		visitors.forEach(v -> v.visitCtAssert(asserted));
		super.visitCtAssert(asserted);
	}

	@Override
	public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignement) {
		visitors.forEach(v -> v.visitCtAssignment(assignement));
		super.visitCtAssignment(assignement);
	}

	@Override
	public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
		visitors.forEach(v -> v.visitCtBinaryOperator(operator));
		super.visitCtBinaryOperator(operator);
	}

	@Override
	public <R> void visitCtBlock(CtBlock<R> block) {
		visitors.forEach(v -> v.visitCtBlock(block));
		super.visitCtBlock(block);
	}

	@Override
	public void visitCtBreak(CtBreak breakStatement) {
		visitors.forEach(v -> v.visitCtBreak(breakStatement));
		super.visitCtBreak(breakStatement);
	}

	@Override
	public <S> void visitCtCase(CtCase<S> caseStatement) {
		visitors.forEach(v -> v.visitCtCase(caseStatement));
		super.visitCtCase(caseStatement);
	}

	@Override
	public void visitCtCatch(CtCatch catchBlock) {
		visitors.forEach(v -> v.visitCtCatch(catchBlock));
		super.visitCtCatch(catchBlock);
	}

	@Override
	public <T> void visitCtClass(CtClass<T> ctClass) {
		SourcePosition pos = ctClass.getPosition();
		if (
			pos != null &&
			pos.isValidPosition() &&
			pos.getEndLine() - pos.getLine() > this.options.getMaxClassLines()
		) {
			logger.info("Skipping the analysis of {} [{} lines > {} lines authorized]",
				ctClass.getQualifiedName(), pos.getEndLine() - pos.getLine(), this.options.getMaxClassLines());
			return;
		}

		visitors.forEach(v -> v.visitCtClass(ctClass));
		super.visitCtClass(ctClass);
	}

	@Override
	public void visitCtTypeParameter(CtTypeParameter typeParameter) {
		visitors.forEach(v -> v.visitCtTypeParameter(typeParameter));
		super.visitCtTypeParameter(typeParameter);
	}

	@Override
	public <T> void visitCtConditional(CtConditional<T> conditional) {
		visitors.forEach(v -> v.visitCtConditional(conditional));
		super.visitCtConditional(conditional);
	}

	@Override
	public <T> void visitCtConstructor(CtConstructor<T> c) {
		visitors.forEach(v -> v.visitCtConstructor(c));
		super.visitCtConstructor(c);
	}

	@Override
	public void visitCtContinue(CtContinue continueStatement) {
		visitors.forEach(v -> v.visitCtContinue(continueStatement));
		super.visitCtContinue(continueStatement);
	}

	@Override
	public void visitCtDo(CtDo doLoop) {
		visitors.forEach(v -> v.visitCtDo(doLoop));
		super.visitCtDo(doLoop);
	}

	@Override
	public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctEnum) {
		visitors.forEach(v -> v.visitCtEnum(ctEnum));
		super.visitCtEnum(ctEnum);
	}

	@Override
	public <T> void visitCtExecutableReference(CtExecutableReference<T> reference) {
		visitors.forEach(v -> v.visitCtExecutableReference(reference));
		super.visitCtExecutableReference(reference);
	}

	@Override
	public <T> void visitCtField(CtField<T> f) {
		visitors.forEach(v -> v.visitCtField(f));
		super.visitCtField(f);
	}

	@Override
	public <T> void visitCtEnumValue(CtEnumValue<T> enumValue) {
		visitors.forEach(v -> v.visitCtEnumValue(enumValue));
		super.visitCtEnumValue(enumValue);
	}

	@Override
	public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
		visitors.forEach(v -> v.visitCtThisAccess(thisAccess));
		super.visitCtThisAccess(thisAccess);
	}

	@Override
	public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
		visitors.forEach(v -> v.visitCtFieldReference(reference));
		super.visitCtFieldReference(reference);
	}

	@Override
	public <T> void visitCtUnboundVariableReference(CtUnboundVariableReference<T> reference) {
		visitors.forEach(v -> v.visitCtUnboundVariableReference(reference));
		super.visitCtUnboundVariableReference(reference);
	}

	@Override
	public void visitCtFor(CtFor forLoop) {
		visitors.forEach(v -> v.visitCtFor(forLoop));
		super.visitCtFor(forLoop);
	}

	@Override
	public void visitCtForEach(CtForEach foreach) {
		visitors.forEach(v -> v.visitCtForEach(foreach));
		super.visitCtForEach(foreach);
	}

	@Override
	public void visitCtIf(CtIf ifElement) {
		visitors.forEach(v -> v.visitCtIf(ifElement));
		super.visitCtIf(ifElement);
	}

	@Override
	public <T> void visitCtInterface(CtInterface<T> intrface) {
		visitors.forEach(v -> v.visitCtInterface(intrface));
		super.visitCtInterface(intrface);
	}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		visitors.forEach(v -> v.visitCtInvocation(invocation));
		super.visitCtInvocation(invocation);
	}

	@Override
	public <T> void visitCtLiteral(CtLiteral<T> literal) {
		visitors.forEach(v -> v.visitCtLiteral(literal));
		super.visitCtLiteral(literal);
	}

	@Override
	public void visitCtTextBlock(CtTextBlock ctTextBlock) {
		visitors.forEach(v -> v.visitCtTextBlock(ctTextBlock));
		super.visitCtTextBlock(ctTextBlock);
	}

	@Override
	public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
		visitors.forEach(v -> v.visitCtLocalVariable(localVariable));
		super.visitCtLocalVariable(localVariable);
	}

	@Override
	public <T> void visitCtLocalVariableReference(CtLocalVariableReference<T> reference) {
		visitors.forEach(v -> v.visitCtLocalVariableReference(reference));
		super.visitCtLocalVariableReference(reference);
	}

	@Override
	public <T> void visitCtCatchVariable(CtCatchVariable<T> catchVariable) {
		visitors.forEach(v -> v.visitCtCatchVariable(catchVariable));
		super.visitCtCatchVariable(catchVariable);
	}

	@Override
	public <T> void visitCtCatchVariableReference(CtCatchVariableReference<T> reference) {
		visitors.forEach(v -> v.visitCtCatchVariableReference(reference));
		super.visitCtCatchVariableReference(reference);
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		visitors.forEach(v -> v.visitCtMethod(m));
		super.visitCtMethod(m);
	}

	@Override
	public <T> void visitCtAnnotationMethod(CtAnnotationMethod<T> annotationMethod) {
		visitors.forEach(v -> v.visitCtAnnotationMethod(annotationMethod));
		super.visitCtAnnotationMethod(annotationMethod);
	}

	@Override
	public <T> void visitCtNewArray(CtNewArray<T> newArray) {
		visitors.forEach(v -> v.visitCtNewArray(newArray));
		super.visitCtNewArray(newArray);
	}

	@Override
	public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
		visitors.forEach(v -> v.visitCtConstructorCall(ctConstructorCall));
		super.visitCtConstructorCall(ctConstructorCall);
	}

	@Override
	public <T> void visitCtNewClass(CtNewClass<T> newClass) {
		visitors.forEach(v -> v.visitCtNewClass(newClass));
		super.visitCtNewClass(newClass);
	}

	@Override
	public <T> void visitCtLambda(CtLambda<T> lambda) {
		visitors.forEach(v -> v.visitCtLambda(lambda));
		super.visitCtLambda(lambda);
	}

	@Override
	public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(CtExecutableReferenceExpression<T, E> expression) {
		visitors.forEach(v -> v.visitCtExecutableReferenceExpression(expression));
		super.visitCtExecutableReferenceExpression(expression);
	}

	@Override
	public <T, A extends T> void visitCtOperatorAssignment(CtOperatorAssignment<T, A> assignment) {
		visitors.forEach(v -> v.visitCtOperatorAssignment(assignment));
		super.visitCtOperatorAssignment(assignment);
	}

	@Override
	public void visitCtPackage(CtPackage ctPackage) {
		visitors.forEach(v -> v.visitCtPackage(ctPackage));
		super.visitCtPackage(ctPackage);
	}

	@Override
	public void visitCtPackageReference(CtPackageReference reference) {
		visitors.forEach(v -> v.visitCtPackageReference(reference));
		super.visitCtPackageReference(reference);
	}

	@Override
	public <T> void visitCtParameter(CtParameter<T> parameter) {
		visitors.forEach(v -> v.visitCtParameter(parameter));
		super.visitCtParameter(parameter);
	}

	@Override
	public <T> void visitCtParameterReference(CtParameterReference<T> reference) {
		visitors.forEach(v -> v.visitCtParameterReference(reference));
		super.visitCtParameterReference(reference);
	}

	@Override
	public <R> void visitCtReturn(CtReturn<R> returnStatement) {
		visitors.forEach(v -> v.visitCtReturn(returnStatement));
		super.visitCtReturn(returnStatement);
	}

	@Override
	public void visitCtStatementList(CtStatementList statements) {
		visitors.forEach(v -> v.visitCtStatementList(statements));
		super.visitCtStatementList(statements);
	}

	@Override
	public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
		visitors.forEach(v -> v.visitCtSwitch(switchStatement));
		super.visitCtSwitch(switchStatement);
	}

	@Override
	public <T, S> void visitCtSwitchExpression(CtSwitchExpression<T, S> switchExpression) {
		visitors.forEach(v -> v.visitCtSwitchExpression(switchExpression));
		super.visitCtSwitchExpression(switchExpression);
	}

	@Override
	public void visitCtSynchronized(CtSynchronized synchro) {
		visitors.forEach(v -> v.visitCtSynchronized(synchro));
		super.visitCtSynchronized(synchro);
	}

	@Override
	public void visitCtThrow(CtThrow throwStatement) {
		visitors.forEach(v -> v.visitCtThrow(throwStatement));
		super.visitCtThrow(throwStatement);
	}

	@Override
	public void visitCtTry(CtTry tryBlock) {
		visitors.forEach(v -> v.visitCtTry(tryBlock));
		super.visitCtTry(tryBlock);
	}

	@Override
	public void visitCtTryWithResource(CtTryWithResource tryWithResource) {
		visitors.forEach(v -> v.visitCtTryWithResource(tryWithResource));
		super.visitCtTryWithResource(tryWithResource);
	}

	@Override
	public void visitCtTypeParameterReference(CtTypeParameterReference ref) {
		visitors.forEach(v -> v.visitCtTypeParameterReference(ref));
		super.visitCtTypeParameterReference(ref);
	}

	@Override
	public void visitCtWildcardReference(CtWildcardReference wildcardReference) {
		visitors.forEach(v -> v.visitCtWildcardReference(wildcardReference));
		super.visitCtWildcardReference(wildcardReference);
	}

	@Override
	public <T> void visitCtIntersectionTypeReference(CtIntersectionTypeReference<T> reference) {
		visitors.forEach(v -> v.visitCtIntersectionTypeReference(reference));
		super.visitCtIntersectionTypeReference(reference);
	}

	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
		visitors.forEach(v -> v.visitCtTypeReference(reference));
		super.visitCtTypeReference(reference);
	}

	@Override
	public <T> void visitCtTypeAccess(CtTypeAccess<T> typeAccess) {
		visitors.forEach(v -> v.visitCtTypeAccess(typeAccess));
		super.visitCtTypeAccess(typeAccess);
	}

	@Override
	public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
		visitors.forEach(v -> v.visitCtUnaryOperator(operator));
		super.visitCtUnaryOperator(operator);
	}

	@Override
	public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
		visitors.forEach(v -> v.visitCtVariableRead(variableRead));
		super.visitCtVariableRead(variableRead);
	}

	@Override
	public <T> void visitCtVariableWrite(CtVariableWrite<T> variableWrite) {
		visitors.forEach(v -> v.visitCtVariableWrite(variableWrite));
		super.visitCtVariableWrite(variableWrite);
	}

	@Override
	public void visitCtWhile(CtWhile whileLoop) {
		visitors.forEach(v -> v.visitCtWhile(whileLoop));
		super.visitCtWhile(whileLoop);
	}

	@Override
	public <T> void visitCtAnnotationFieldAccess(CtAnnotationFieldAccess<T> annotationFieldAccess) {
		visitors.forEach(v -> v.visitCtAnnotationFieldAccess(annotationFieldAccess));
		super.visitCtAnnotationFieldAccess(annotationFieldAccess);
	}

	@Override
	public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
		visitors.forEach(v -> v.visitCtFieldRead(fieldRead));
		super.visitCtFieldRead(fieldRead);
	}

	@Override
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		visitors.forEach(v -> v.visitCtFieldWrite(fieldWrite));
		super.visitCtFieldWrite(fieldWrite);
	}

	@Override
	public <T> void visitCtSuperAccess(CtSuperAccess<T> f) {
		visitors.forEach(v -> v.visitCtSuperAccess(f));
		super.visitCtSuperAccess(f);
	}

	@Override
	public void visitCtComment(CtComment comment) {
		visitors.forEach(v -> v.visitCtComment(comment));
		super.visitCtComment(comment);
	}

	@Override
	public void visitCtJavaDoc(CtJavaDoc comment) {
		visitors.forEach(v -> v.visitCtJavaDoc(comment));
		super.visitCtJavaDoc(comment);
	}

	@Override
	public void visitCtJavaDocTag(CtJavaDocTag docTag) {
		visitors.forEach(v -> v.visitCtJavaDocTag(docTag));
		super.visitCtJavaDocTag(docTag);
	}

	@Override
	public void visitCtImport(CtImport ctImport) {
		visitors.forEach(v -> v.visitCtImport(ctImport));
		super.visitCtImport(ctImport);
	}

	@Override
	public void visitCtModule(CtModule module) {
		visitors.forEach(v -> v.visitCtModule(module));
		super.visitCtModule(module);
	}

	@Override
	public void visitCtModuleReference(CtModuleReference moduleReference) {
		visitors.forEach(v -> v.visitCtModuleReference(moduleReference));
		super.visitCtModuleReference(moduleReference);
	}

	@Override
	public void visitCtPackageExport(CtPackageExport moduleExport) {
		visitors.forEach(v -> v.visitCtPackageExport(moduleExport));
		super.visitCtPackageExport(moduleExport);
	}

	@Override
	public void visitCtModuleRequirement(CtModuleRequirement moduleRequirement) {
		visitors.forEach(v -> v.visitCtModuleRequirement(moduleRequirement));
		super.visitCtModuleRequirement(moduleRequirement);
	}

	@Override
	public void visitCtProvidedService(CtProvidedService moduleProvidedService) {
		visitors.forEach(v -> v.visitCtProvidedService(moduleProvidedService));
		super.visitCtProvidedService(moduleProvidedService);
	}

	@Override
	public void visitCtUsedService(CtUsedService usedService) {
		visitors.forEach(v -> v.visitCtUsedService(usedService));
		super.visitCtUsedService(usedService);
	}

	@Override
	public void visitCtCompilationUnit(CtCompilationUnit compilationUnit) {
		visitors.forEach(v -> v.visitCtCompilationUnit(compilationUnit));
		super.visitCtCompilationUnit(compilationUnit);
	}

	@Override
	public void visitCtPackageDeclaration(CtPackageDeclaration packageDeclaration) {
		visitors.forEach(v -> v.visitCtPackageDeclaration(packageDeclaration));
		super.visitCtPackageDeclaration(packageDeclaration);
	}

	@Override
	public void visitCtTypeMemberWildcardImportReference(CtTypeMemberWildcardImportReference wildcardReference) {
		visitors.forEach(v -> v.visitCtTypeMemberWildcardImportReference(wildcardReference));
		super.visitCtTypeMemberWildcardImportReference(wildcardReference);
	}

	@Override
	public void visitCtYieldStatement(CtYieldStatement statement) {
		visitors.forEach(v -> v.visitCtYieldStatement(statement));
		super.visitCtYieldStatement(statement);
	}
}
