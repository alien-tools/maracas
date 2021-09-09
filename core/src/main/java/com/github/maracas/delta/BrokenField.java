package com.github.maracas.delta;

import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.FieldLessAccessibleVisitor;
import com.github.maracas.visitors.FieldNoLongerStaticVisitor;
import com.github.maracas.visitors.FieldNowFinalVisitor;
import com.github.maracas.visitors.FieldNowStaticVisitor;
import com.github.maracas.visitors.FieldRemovedVisitor;
import com.github.maracas.visitors.FieldTypeChangedVisitor;

import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiField;
import javassist.NotFoundException;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class BrokenField extends AbstractBrokenDeclaration {
	private final JApiField jApiField;
	private final CtFieldReference<?> fRef;

	public BrokenField(JApiField field, CtFieldReference<?> fRef, JApiCompatibilityChange change) {
		super(change);
		this.jApiField = field;
		this.fRef = fRef;
	}

	@Override
	public CtReference getReference() {
		return fRef;
	}

	@Override
	public BreakingChangeVisitor getVisitor() {
		return
			switch (change) {
				case FIELD_REMOVED          -> new FieldRemovedVisitor(fRef);
				case FIELD_NOW_FINAL        -> new FieldNowFinalVisitor(fRef);
				case FIELD_NO_LONGER_STATIC -> new FieldNoLongerStaticVisitor(fRef);
				case FIELD_NOW_STATIC       -> new FieldNowStaticVisitor(fRef);
				case FIELD_LESS_ACCESSIBLE  -> new FieldLessAccessibleVisitor(fRef, jApiField.getAccessModifier().getNewModifier().get());
				case FIELD_TYPE_CHANGED     -> {
					try {
						// Thanks for the checked exception
						String newTypeName = jApiField.getNewFieldOptional().get().getType().getName();
						CtTypeReference<?> newType = fRef.getFactory().Type().createReference(newTypeName);
						yield new FieldTypeChangedVisitor(fRef, newType);
					} catch (NotFoundException e) {
						yield null;
					}
				}
				default -> null;
			};
	}
}
