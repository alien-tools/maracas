package org.swat.maracas.spoon.delta;

import org.swat.maracas.spoon.visitors.BreakingChangeVisitor;
import org.swat.maracas.spoon.visitors.FieldLessAccessibleVisitor;
import org.swat.maracas.spoon.visitors.FieldNoLongerStaticVisitor;
import org.swat.maracas.spoon.visitors.FieldNowFinalVisitor;
import org.swat.maracas.spoon.visitors.FieldNowStaticVisitor;
import org.swat.maracas.spoon.visitors.FieldRemovedVisitor;
import org.swat.maracas.spoon.visitors.FieldTypeChangedVisitor;

import japicmp.model.JApiCompatibilityChange;
import japicmp.model.JApiField;
import javassist.NotFoundException;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class BrokenField implements BrokenDeclaration {
	private final JApiField jApiField;
	private final CtFieldReference<?> fRef;
	private final JApiCompatibilityChange change;

	public BrokenField(JApiField field, CtFieldReference<?> fRef, JApiCompatibilityChange change) {
		this.jApiField = field;
		this.fRef = fRef;
		this.change = change;
	}

	@Override
	public CtReference getReference() {
		return fRef;
	}

	@Override
	public JApiCompatibilityChange getChange() {
		return change;
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
