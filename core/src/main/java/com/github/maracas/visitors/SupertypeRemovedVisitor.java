package com.github.maracas.visitors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.maracas.brokenUse.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.SpoonException;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * Visitor in charge of gathering all supertype removed issues in client code.
 *
 * The visitor detects the following cases:
 * <ul>
 * <li>Methods overriding methods declared within the supertype. Example:
 *
 * <pre>
 * &#64;Override
 * public void m() {
 *     return;
 * }
 * </pre>
 *
 * <li>Accessing supertype fields via subtypes. Example:
 *
 * <pre>
 *      AffectedSubtype.field;
 * </pre>
 *
 * <li>Invoking supertype methods via subtypes. Example:
 *
 * <pre>
 * AffectedSubtype.method();
 * </pre>
 *
 * <li>Casting local variables with removed supertype. Example:
 *
 * <pre>
 * RemovedSupertype s = (RemovedSupertype) subtypeObj;
 * </pre>
 * </ul>
 */
public class SupertypeRemovedVisitor extends BreakingChangeVisitor {
    /**
     * Spoon reference to the class that removed the supertype(s).
     */
    protected final CtTypeReference<?> clsRef;

    /**
     * Set of removed supertypes of the class (interfaces and classes).
     */
    protected final Set<CtTypeReference<?>> supertypes;

    /**
     * Set of methods defined within the removed supertypes.
     */
    protected final Set<CtExecutableReference<?>> superMethods;

    /**
     * Set of field defined within the removed supertypes.
     */
    protected final Set<String> superFields;

    /**
     * Creates a SupertypeRemovedVisitor instance.
     *
     * @param clsRef     class that removed the supertype(s)
     * @param supertypes set of removed supertypes
     * @param change     kind of breaking change (interface removed or superclass
     *                   removed)
     */
    protected SupertypeRemovedVisitor(CtTypeReference<?> clsRef, Set<CtTypeReference<?>> supertypes,
        JApiCompatibilityChange change) {
        super(change);
        this.clsRef = clsRef;
        this.supertypes = supertypes;
        this.superMethods = supertypes.stream().map(i -> i.getDeclaredExecutables()).flatMap(Collection::stream)
            .collect(Collectors.toSet());
        this.superFields = supertypes.stream().map(i -> i.getDeclaredFields()).flatMap(Collection::stream)
            .map(f -> f.getSimpleName()).collect(Collectors.toSet());
    }

    @Override
    public <T> void visitCtFieldReference(CtFieldReference<T> fieldRef) {
        CtTypeReference<?> typeRef = fieldRef.getDeclaringType();
        try {
            if (typeRef != null && typeRef.isSubtypeOf(clsRef)) {
                CtFieldReference<?> declRef = typeRef.getDeclaredField(fieldRef.getSimpleName());

                if (declRef == null && superFields.contains(fieldRef.getSimpleName()))
                    brokenUse(fieldRef, fieldRef, clsRef, APIUse.FIELD_ACCESS);
            }
        } catch(SpoonException e) {
            // FIXME: Find fancier solution. A declaration cannot be resolved
        }
    }

    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        CtTypeReference<?> typeRef = ((CtType<?>) invocation.getParent(CtType.class)).getReference();

        try {
            if (typeRef.isSubtypeOf(clsRef)) {
                CtExecutableReference<?> methRef = invocation.getExecutable();

                if (superMethods.contains(methRef))
                    brokenUse(invocation, methRef, clsRef, APIUse.METHOD_INVOCATION);
            }
        } catch (SpoonException e) {
            // FIXME: Find fancier solution. A declaration cannot be resolved
        }

        // FIXME: cases where a static access is performed via the supertype
        // must not be registered as a broken use.

        // var target = invocation.getTarget();
        // if (methRef.isStatic() && target instanceof CtTypeAccess<?>
        // && supertypes.contains(((CtTypeAccess<?>) target).getAccessedType()))
        // return;
    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {
        try {
            if (m.getDeclaringType().isSubtypeOf(clsRef)) {
                CtExecutableReference<?> superMeth = m.getReference().getOverridingExecutable();

                if (superMeth != null && superMethods.contains(superMeth))
                    brokenUse(m, superMeth, clsRef, APIUse.METHOD_OVERRIDE);
            }
        } catch (SpoonException e) {
            // A declaration cannot be resolved
            // FIXME: deal with this issue in a fancier way?
        }
    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignment) {
        visitExpAssignment(assignment.getAssignment());
    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        visitExpAssignment(localVariable.getAssignment());
    }

    /**
     * Visits an assignment expression and adds a new broken use if the class of the
     * object is a subtype of the removed supertype.
     *
     * @param <T>        type of the expression
     * @param assignExpr assignment expression
     */
    private <T> void visitExpAssignment(CtExpression<T> assignExpr) {
        // FIXME: when dealing with interfaces this issue is not reported
        // as a compilation error.
        if (assignExpr != null) {
            Set<CtTypeReference<?>> casts = new HashSet<CtTypeReference<?>>(assignExpr.getTypeCasts());
            CtTypeReference<?> typeRef = assignExpr.getType();

            for (CtTypeReference<?> cast : casts) {
                if (supertypes.contains(cast) && typeRef.isSubtypeOf(clsRef))
                    brokenUse(assignExpr, cast, clsRef, APIUse.TYPE_DEPENDENCY);
            }
        }
    }
}
