package com.github.maracas.visitors;

import java.util.HashSet;
import java.util.Set;

import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.util.SpoonHelpers;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtAbstractVisitor;

public abstract class BreakingChangeVisitor extends CtAbstractVisitor {
	private final JApiCompatibilityChange change;
	private final Set<BrokenUse> brokenUses = new HashSet<>();

	protected BreakingChangeVisitor(JApiCompatibilityChange change) {
		super();
		this.change = change;
	}

	protected void brokenUse(CtElement element, CtElement usedApiElement, CtReference source, APIUse use) {
		// We don't want to create broken uses for implicit elements: they do not
		// exist in the source code of the client anyway
		if (element.isImplicit())
			return;

		// In case we don't get a source code position for the element, we default
		// to the first parent that can be located
		CtElement locatableElement =
			element.getPosition() instanceof NoSourcePosition ?
				SpoonHelpers.firstLocatableParent(element) :
				element;

		BrokenUse d = new BrokenUse(
			locatableElement,
			usedApiElement,
			source,
			use,
			change
		);

		brokenUses.add(d);
	}

	public APIUse getAPIUseByRole(CtElement element) {
		CtRole role = element.getRoleInParent();
		return switch (role) {
			// FIXME: try to distinguish between regular access to a type,
			// and access to a type by instantiation (new)
			case CAST, DECLARING_TYPE, TYPE, ARGUMENT_TYPE, ACCESSED_TYPE, TYPE_ARGUMENT, THROWN, MULTI_TYPE ->
				APIUse.TYPE_DEPENDENCY;
			case SUPER_TYPE ->
				APIUse.EXTENDS;
			case INTERFACE ->
				APIUse.IMPLEMENTS;
			case ANNOTATION_TYPE ->
				APIUse.ANNOTATION;
			case IMPORT_REFERENCE ->
				APIUse.IMPORT;
			case DECLARED_TYPE_REF ->
				APIUse.TYPE_DEPENDENCY; // FIXME: This one is weird
			default ->
				throw new RuntimeException("Unmanaged role " + role + " for " + element + " in " + element.getParent());
		};
	}

	public Set<BrokenUse> getBrokenUses() {
		return brokenUses;
	}
}
