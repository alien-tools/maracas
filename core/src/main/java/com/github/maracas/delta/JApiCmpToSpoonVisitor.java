package com.github.maracas.delta;

import com.github.maracas.util.SpoonHelpers;
import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class JApiCmpToSpoonVisitor implements JApiCmpDeltaVisitor {
  private final CtPackage root;
  private final Collection<BreakingChange> breakingChanges = new ArrayList<>();

  public JApiCmpToSpoonVisitor(CtPackage root) {
    this.root = root;
  }

  public Collection<BreakingChange> getBreakingChanges() {
    return breakingChanges;
  }

  @Override
  public void visit(JApiClass cls) {
    CtTypeReference<?> clsRef = root.getFactory().Type().createReference(cls.getFullyQualifiedName());
    if (clsRef != null) {
      cls.getCompatibilityChanges().forEach(c ->
        breakingChanges.add(new ClassBreakingChange(cls, clsRef, c))
      );

      cls.getInterfaces().forEach(i ->
        visit(cls, i)
      );
    }
  }

  @Override
  public void visit(JApiMethod m) {
    CtTypeReference<?> clsRef = root.getFactory().Type().createReference(m.getjApiClass().getFullyQualifiedName());
    var oldMethodOpt = m.getOldMethod();
    var newMethodOpt = m.getNewMethod();

    if (oldMethodOpt.isPresent()) {
      CtMethod oldMethod = oldMethodOpt.get();
      String sign = SpoonHelpers.buildSpoonSignature(m);
      CtExecutableReference<?> mRef = root.getFactory().Method().createReference(sign);

      try {
        if (mRef != null && mRef.getExecutableDeclaration() != null) {
          m.getCompatibilityChanges().forEach(c ->
            breakingChanges.add(new MethodBreakingChange(m, mRef, c))
          );
        } else {
          if (oldMethod.getName().equals("values") || oldMethod.getName().equals("valueOf")) {
            // When an enum is transformed into anything else,
            // japicmp reports that valueOf(String)/values() are removed
            // Ignore. FIXME
            ;
          } else {
            System.out.println("Couldn't find old method " + m);
          }
        }
      } catch (Exception e) {
        System.out.println("Couldn't resolve method " + m + " [" + e.getMessage() + "]");
      }
    } else if (newMethodOpt.isPresent()) {
      // Added method introducing a breaking change.
      CtMethod newMethod = newMethodOpt.get();

      // FIXME: we miss the information about the newly added method
      if (!(newMethod.getName().equals("values") || newMethod.getName().equals("valueOf"))) {
        m.getCompatibilityChanges().forEach(c ->
          breakingChanges.add(new ClassBreakingChange(m.getjApiClass(), clsRef, c))
        );
      }
    } else {
      throw new RuntimeException("The JApiCmp delta model is corrupted.");
    }
  }

  @Override
  public void visit(JApiField f) {
    CtTypeReference<?> clsRef = root.getFactory().Type().createReference(f.getjApiClass().getFullyQualifiedName());
    var oldFieldOpt = f.getOldFieldOptional();
    if (oldFieldOpt.isPresent()) {
      CtField oldField = oldFieldOpt.get();
      CtFieldReference<?> fRef = clsRef.getDeclaredField(oldField.getName());

      if (fRef != null && fRef.getFieldDeclaration() != null)
        f.getCompatibilityChanges().forEach(c ->
          breakingChanges.add(new FieldBreakingChange(f, fRef, c))
        );
    } else {
      // No oldField
    }
  }

  @Override
  public void visit(JApiConstructor cons) {
    CtTypeReference<?> clsRef = root.getFactory().Type().createReference(cons.getjApiClass().getFullyQualifiedName());
    var oldConsOpt = cons.getOldConstructor();

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

      if (cRefOpt.isPresent()) {
        cons.getCompatibilityChanges().forEach(c ->
          breakingChanges.add(new MethodBreakingChange(cons, cRefOpt.get(), c)));
      } else {
        System.out.println("Couldn't find constructor " + cons);
      }

      // FIXME: Once the issue with the signature is found,
      // uncomment this code.
//                    String sign = SpoonHelpers.buildSpoonSignature(cons);
//                    CtExecutableReference<?> consRef = root.getFactory().Constructor().createReference(sign);
//
//                    if (consRef != null && consRef.getExecutableDeclaration() != null) {
//                        cons.getCompatibilityChanges().forEach(c ->
//                          breakingChanges.add(new MethodBreakingChange(cons, consRef, c))
//                        );
//                    } else {
//                        // No old constructor
//                        System.out.println("Couldn't find constructor " + cons);
//                    }
    }
  }

  @Override
  public void visit(JApiImplementedInterface intf) {
    // Using visit(JApiClass jApiClass, JApiImplementedInterface jApiImplementedInterface)
    // FIXME: is there a way to get the JApiClass from the interface?
  }

  @Override
  public void visit(JApiAnnotation ann) {
  }

  @Override
  public void visit(JApiSuperclass superCls) {
    JApiClass jApiClass = superCls.getJApiClassOwning();
    CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiClass.getFullyQualifiedName());

    if (clsRef != null && clsRef.getTypeDeclaration() != null)
      superCls.getCompatibilityChanges().forEach(c ->
        breakingChanges.add(new ClassBreakingChange(jApiClass, clsRef, c))
      );
  }

  public void visit(JApiClass cls, JApiImplementedInterface intf) {
    CtTypeReference<?> clsRef = root.getFactory().Type().createReference(cls.getFullyQualifiedName());

    if (clsRef != null && clsRef.getTypeDeclaration() != null)
      intf.getCompatibilityChanges().forEach(c ->
        breakingChanges.add(new ClassBreakingChange(cls, clsRef, c))
      );
  }
}
