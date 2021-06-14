package org.swat.maracas.spoon;

import java.util.List;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtFieldReference;

public class TryingStuff {
	public static List<CtFieldWrite<?>> findFieldWrite(CtModel model, String fieldDeclaringType, String fieldName) {
		CtPackage root = model.getRootPackage();

		return root.filterChildren((CtFieldWrite<?> f) -> {
			CtFieldReference<?> field = f.getVariable();
			String name = field.getSimpleName();
			String declaringType = field.getDeclaringType().getQualifiedName();

			if (fieldName.equals(name) && fieldDeclaringType.equals(declaringType))
				return true;

			return false;
		}).list();
	}
}
