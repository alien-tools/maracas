package org.swat.maracas.spoon.visitors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import japicmp.output.Filter.FilterVisitor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class DeltaVisitor implements FilterVisitor {
	private final CtPackage root;
	private final List<BreakingChangeVisitor> visitors = new ArrayList<>();

	public DeltaVisitor(CtPackage root) {
		this.root = root;
	}

	public List<BreakingChangeVisitor> getVisitors() {
		return visitors;
	}

	@Override
	public void visit(Iterator<JApiClass> iterator, JApiClass elem) {
		CtTypeReference<?> clsRef = root.getFactory().Type().createReference(elem.getFullyQualifiedName());
		elem.getCompatibilityChanges().forEach(c -> {
			BreakingChangeVisitor visitor = switch (c) {
				case CLASS_NO_LONGER_PUBLIC      -> null; // CLASS_LESS_ACCESSIBLE is a superset of CLASS_LESS_ACCESSIBLE; fix japicmp
				case CLASS_LESS_ACCESSIBLE       -> new ClassLessAccessibleVisitor(clsRef, elem.getAccessModifier().getNewModifier().get());
				case CLASS_NOW_ABSTRACT          -> new ClassNowAbstractVisitor(clsRef);
				case CLASS_NOW_FINAL             -> new ClassNowFinalVisitor(clsRef);
				case CLASS_NOW_CHECKED_EXCEPTION -> new ClassNowCheckedExceptionVisitor(clsRef);
				case ANNOTATION_DEPRECATED_ADDED -> new AnnotationDeprecatedAddedVisitor(clsRef);
				case CLASS_REMOVED               -> new ClassRemovedVisitor(clsRef);
				default -> null;
			};

			if (visitor != null)
				visitors.add(visitor);
		});
	}

	@Override
	public void visit(Iterator<JApiMethod> iterator, JApiMethod elem) {
		CtTypeReference<?> clsRef = root.getFactory().Type().createReference(elem.getjApiClass().getFullyQualifiedName());
		elem.getCompatibilityChanges().forEach(c -> {
			japicmp.util.Optional<CtMethod> oldMethodOpt = elem.getOldMethod();
			if (oldMethodOpt.isPresent()) {
				CtMethod oldMethod = oldMethodOpt.get();

				Optional<CtExecutableReference<?>> mRefOpt =
					clsRef.getDeclaredExecutables()
					.stream()
					.filter(m -> matchingSignatures(m, oldMethod))
					.findFirst();

				if (mRefOpt.isPresent()) {
					BreakingChangeVisitor visitor = switch (c) {
						case METHOD_REMOVED      -> new MethodRemovedVisitor(mRefOpt.get());
						case METHOD_NOW_FINAL    -> new MethodNowFinalVisitor(mRefOpt.get());
						case METHOD_NOW_ABSTRACT -> new MethodNowAbstractVisitor(mRefOpt.get());
						default -> null;
					};

					if (visitor != null)
						visitors.add(visitor);
				} else {
					if (oldMethod.getName().equals("values") || oldMethod.getName().equals("valueOf"))
						// When an enum is transformed into anything else,
						// japicmp reports that valueOf(String)/values() are removed
						// Ignore.
						;
					else
						throw new RuntimeException("Unmatched " + oldMethod);
				}
			} else {
				// No oldMethod
			}
		});
	}

	@Override
	public void visit(Iterator<JApiConstructor> iterator, JApiConstructor elem) {
	}

	@Override
	public void visit(Iterator<JApiImplementedInterface> iterator, JApiImplementedInterface elem) {
	}

	@Override
	public void visit(Iterator<JApiField> iterator, JApiField elem) {
		CtTypeReference<?> clsRef = root.getFactory().Type().createReference(elem.getjApiClass().getFullyQualifiedName());
		elem.getCompatibilityChanges().forEach(c -> {
			japicmp.util.Optional<CtField> oldFieldOpt = elem.getOldFieldOptional();
			if (oldFieldOpt.isPresent()) {
				CtField oldField = oldFieldOpt.get();
				CtFieldReference<?> fRef = clsRef.getDeclaredField(oldField.getName());

				BreakingChangeVisitor visitor = switch (c) {
					case FIELD_NOW_FINAL        -> new FieldNowFinalVisitor(fRef);
					case FIELD_NO_LONGER_STATIC -> new FieldNoLongerStaticVisitor(fRef);
					case FIELD_NOW_STATIC       -> new FieldNowStaticVisitor(fRef);
					case FIELD_LESS_ACCESSIBLE  -> new FieldLessAccessibleVisitor(fRef, elem.getAccessModifier().getNewModifier().get());
					case FIELD_TYPE_CHANGED     -> {
						try {
							// Thanks for the checked exception
							String newTypeName = elem.getNewFieldOptional().get().getType().getName();
							CtTypeReference<?> newType = root.getFactory().Type().createReference(newTypeName);
							yield new FieldTypeChangedVisitor(fRef, newType);
						} catch (NotFoundException e) {
							yield null;
						}
					}
					default -> null;
				};

				if (visitor != null)
					visitors.add(visitor);
			} else {
				// No oldMethod
			}
		});
	}

	@Override
	public void visit(Iterator<JApiAnnotation> iterator, JApiAnnotation elem) {
	}

	@Override
	public void visit(JApiSuperclass elem) {
	}

	private boolean matchingSignatures(CtExecutableReference<?> spoonMethod, CtMethod japiMethod) {
		return
			japiMethod.getName().concat(japiMethod.getSignature()).startsWith(spoonMethod.getSignature());
	}
}
