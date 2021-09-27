package com.github.maracas.delta;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.maracas.SpoonHelper;
import com.github.maracas.visitors.BreakingChangeVisitor;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import japicmp.output.Filter;
import japicmp.output.Filter.FilterVisitor;
import javassist.CtField;
import javassist.CtMethod;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class Delta {
	private final Path v1;
	private final Path v2;
	private final List<BrokenDeclaration> brokenDeclarations = new ArrayList<>();

	public Delta(Path v1, Path v2, List<JApiClass> classes) {
		this.v1 = v1;
		this.v2 = v2;
		extractBrokenDeclarations(classes);
	}

	public void extractBrokenDeclarations(List<JApiClass> classes) {
		// We need to create CtReferences to v1 to map japicmp's delta
		// to our own. It seems building an empty model with the right
		// classpath allows us to create these references. FIXME
		Launcher launcher = new Launcher();
		String[] javaCp = { v1.toAbsolutePath().toString() };
		launcher.getEnvironment().setSourceClasspath(javaCp);
		CtModel model = launcher.buildModel();
		CtPackage root = model.getRootPackage();

		// FIXME: Ok, for some reason, @Deprecated methods (and fields? classes?)
		// do not show up in the resulting model. This means that a @Deprecated
		// method that gets removed can't be mapped to the proper CtElement.

		Filter.filter(classes, new FilterVisitor() {
			@Override
			public void visit(Iterator<JApiClass> iterator, JApiClass jApiClass) {
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiClass.getFullyQualifiedName());
				jApiClass.getCompatibilityChanges().forEach(c ->
					brokenDeclarations.add(new BrokenClass(jApiClass, clsRef, c))
				);
			}

			@Override
			public void visit(Iterator<JApiMethod> iterator, JApiMethod jApiMethod) {
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiMethod.getjApiClass().getFullyQualifiedName());
				var oldMethodOpt = jApiMethod.getOldMethod();
				if (oldMethodOpt.isPresent()) {
					CtMethod oldMethod = oldMethodOpt.get();
					Optional<CtExecutableReference<?>> mRefOpt =
						clsRef.getDeclaredExecutables()
						.stream()
						.filter(m -> SpoonHelper.matchingSignatures(m, oldMethod))
						.findFirst();

					if (mRefOpt.isPresent()) {
						jApiMethod.getCompatibilityChanges().forEach(c ->
							brokenDeclarations.add(new BrokenMethod(jApiMethod, mRefOpt.get(), c))
						);
					} else {
						if (oldMethod.getName().equals("values") || oldMethod.getName().equals("valueOf"))
							// When an enum is transformed into anything else,
							// japicmp reports that valueOf(String)/values() are removed
							// Ignore. FIXME
							;
						else {
							System.err.println("Spoon's old method cannot be found: " + jApiMethod);
							System.err.println("\tKnown bug: is the method @Deprecated?");
						}
					}
				} else {
					// No oldMethod
				}
			}

			@Override
			public void visit(Iterator<JApiField> iterator, JApiField jApiField) {
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiField.getjApiClass().getFullyQualifiedName());
				var oldFieldOpt = jApiField.getOldFieldOptional();
				if (oldFieldOpt.isPresent()) {
					CtField oldField = oldFieldOpt.get();
					CtFieldReference<?> fRef = clsRef.getDeclaredField(oldField.getName());

					jApiField.getCompatibilityChanges().forEach(c ->
						brokenDeclarations.add(new BrokenField(jApiField, fRef, c))
					);
				} else {
					// No oldField
				}
			}

			@Override
			public void visit(Iterator<JApiConstructor> iterator, JApiConstructor jApiConstructor) {
			}

			@Override
			public void visit(Iterator<JApiImplementedInterface> iterator, JApiImplementedInterface jApiImplementedInterface) {
			}

			@Override
			public void visit(Iterator<JApiAnnotation> iterator, JApiAnnotation jApiAnnotation) {
			}

			@Override
			public void visit(JApiSuperclass jApiSuperclass) {
			}
		});
	}

	public List<BreakingChangeVisitor> getVisitors() {
		return
				brokenDeclarations.stream()
				.map(BrokenDeclaration::getVisitor)
				.filter(Objects::nonNull) // Temporary; FIXME
				.toList();
	}

	public List<BrokenDeclaration> getBrokenDeclarations() {
		return brokenDeclarations;
	}

	public Path getV1() {
		return v1;
	}

	public Path getV2() {
		return v2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Δ(" + v1 + " -> " + v2 + ")\n");
		sb.append(
			brokenDeclarations.stream()
			.map(bd -> """
				[%s]
					Reference: %s
					Source: %s %s
				""".formatted(bd.getChange(), bd.getReference(),
					bd.getSourceElement() instanceof CtNamedElement ne ? ne.getSimpleName() : bd.getSourceElement(),
					bd.getSourceElement() != null ? bd.getSourceElement().getPosition() : null)
			).collect(Collectors.joining())
		);
		return sb.toString();
	}
}
