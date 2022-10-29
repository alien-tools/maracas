package com.github.maracas.delta;

import com.github.maracas.util.SpoonHelpers;
import japicmp.model.*;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JApiCmpToSpoonVisitor implements JApiCmpDeltaVisitor {
	private final CtPackage root;
	private final List<BreakingChange> breakingChanges = new ArrayList<>();
	private static final Logger logger = LogManager.getLogger(JApiCmpToSpoonVisitor.class);

	public JApiCmpToSpoonVisitor(CtPackage root) {
		this.root = root;
	}

	public List<BreakingChange> getBreakingChanges() {
		return breakingChanges;
	}

	@Override
	public void visit(JApiClass cls) {
		Collection<JApiCompatibilityChange> bcs = cls.getCompatibilityChanges();

		try {
			if (!bcs.isEmpty()) {
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(cls.getFullyQualifiedName());

				if (clsRef != null && clsRef.getTypeDeclaration() != null)
					breakingChanges.addAll(
						bcs.stream().map(c -> new TypeBreakingChange(cls, clsRef, c)).toList()
					);
				else
					logger.warn("Couldn't find Spoon node for class {}", cls);
			}

			cls.getInterfaces().forEach(i ->
				visit(cls, i)
			);
		} catch (NoClassDefFoundError e) {
			logger.error(e);
		}
	}

	@Override
	public void visit(JApiMethod m) {
		Collection<JApiCompatibilityChange> bcs = m.getCompatibilityChanges();

		try {
			if (!bcs.isEmpty()) {
				var oldMethodOpt = m.getOldMethod();
				var newMethodOpt = m.getNewMethod();

				if (oldMethodOpt.isPresent()) {
					String sign = SpoonHelpers.buildSpoonSignature(m);
					CtExecutableReference<?> mRef = root.getFactory().Method().createReference(sign);

					if (mRef != null && mRef.getExecutableDeclaration() != null)
						breakingChanges.addAll(
							bcs.stream().map(c -> new MethodBreakingChange(m, mRef, c)).toList()
						);
					else
						logger.warn("Couldn't find Spoon node for old method {}", m);
				} else if (newMethodOpt.isPresent()) {
					// Added method introducing a breaking change => we attach it to its containing class
					CtMethod newMethod = newMethodOpt.get();

					// Unless this is one of these auto-generated Enum methods
					if (!newMethod.getName().equals("values") && !newMethod.getName().equals("valueOf")) {
						CtTypeReference<?> clsRef = root.getFactory().Type().createReference(m.getjApiClass().getFullyQualifiedName());

						breakingChanges.addAll(
							bcs.stream().map(c -> new TypeBreakingChange(m.getjApiClass(), clsRef, c)).toList()
						);
					}
				}
			}
		} catch (NoClassDefFoundError e) {
			logger.error(e);
		}
	}

	@Override
	public void visit(JApiField f) {
		Collection<JApiCompatibilityChange> bcs = f.getCompatibilityChanges();

		try {
			if (!bcs.isEmpty()) {
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(f.getjApiClass().getFullyQualifiedName());

				if (clsRef != null && clsRef.getTypeDeclaration() != null) {
					var oldFieldOpt = f.getOldFieldOptional();
					if (oldFieldOpt.isPresent()) {
						CtField oldField = oldFieldOpt.get();
						CtFieldReference<?> fRef = clsRef.getDeclaredField(oldField.getName());

						if (fRef != null && fRef.getFieldDeclaration() != null)
							breakingChanges.addAll(
								bcs.stream().map(c -> new FieldBreakingChange(f, fRef, c)).toList()
							);
						else
							logger.warn("Couldn't find Spoon node for old field {}", f);
					} // else => no oldField
				} else
					logger.warn("Couldn't find Spoon node for type {}", f.getjApiClass());
			}
		} catch (NoClassDefFoundError e) {
			logger.error(e);
		}
	}

	@Override
	public void visit(JApiConstructor cons) {
		Collection<JApiCompatibilityChange> bcs = cons.getCompatibilityChanges();

		try {
			if (!bcs.isEmpty()) {
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(cons.getjApiClass().getFullyQualifiedName());
				var oldConsOpt = cons.getOldConstructor();

				if (clsRef != null && clsRef.getTypeDeclaration() != null) {
					// FIXME: Creating a reference out from a constructor signature
					// returns an ExecutableReference with no position. This code
					// needs to go at some point.
					if (oldConsOpt.isPresent()) {
						CtConstructor oldCons = oldConsOpt.get();
						Optional<CtExecutableReference<?>> cRefOpt =
							clsRef.getDeclaredExecutables()
								.stream()
								.filter(c -> SpoonHelpers.matchingSignatures(c, oldCons))
								.findFirst();

						if (cRefOpt.isPresent())
							breakingChanges.addAll(
								bcs.stream().map(c -> new MethodBreakingChange(cons, cRefOpt.get(), c)).toList()
							);
						else
							logger.warn("Couldn't find constructor {}", cons);
					}
				} else
					logger.warn("Couldn't find Spoon node for type {}", cons.getjApiClass());
			}
		} catch (NoClassDefFoundError e) {
			logger.error(e);
		}
	}

	@Override
	public void visit(JApiImplementedInterface intf) {
		// Using visit(JApiClass jApiClass, JApiImplementedInterface jApiImplementedInterface)
	}

	@Override
	public void visit(JApiAnnotation ann) {
	}

	@Override
	public void visit(JApiSuperclass superCls) {
		Collection<JApiCompatibilityChange> bcs = superCls.getCompatibilityChanges();

		try {
			if (!bcs.isEmpty()) {
				JApiClass jApiClass = superCls.getJApiClassOwning();
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiClass.getFullyQualifiedName());

				if (clsRef != null && clsRef.getTypeDeclaration() != null)
					breakingChanges.addAll(
						bcs.stream().map(c -> new TypeBreakingChange(jApiClass, clsRef, c)).toList()
					);
				else
					logger.warn("Couldn't find Spoon node for type {}", jApiClass);
			}
		} catch (NoClassDefFoundError e) {
			logger.error(e);
		}
	}

	public void visit(JApiClass cls, JApiImplementedInterface intf) {
		Collection<JApiCompatibilityChange> bcs = intf.getCompatibilityChanges();

		try {
			if (!bcs.isEmpty()) {
				CtTypeReference<?> clsRef = root.getFactory().Type().createReference(cls.getFullyQualifiedName());

				if (clsRef != null && clsRef.getTypeDeclaration() != null)
					breakingChanges.addAll(
						bcs.stream().map(c -> new TypeBreakingChange(cls, clsRef, c)).toList()
					);
				else
					logger.warn("Couldn't find Spoon node for type {}", cls);
			}
		} catch (NoClassDefFoundError e) {
			logger.error(e);
		}
	}
}
