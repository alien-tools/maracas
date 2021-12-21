package com.github.maracas.delta;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.maracas.util.PathHelpers;
import com.github.maracas.util.SpoonHelpers;
import com.github.maracas.visitors.BreakingChangeVisitor;

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
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * A delta model lists the breaking changes between two versions of a library,
 * represented as a collection of {@link BreakingChange}.
 */
public class Delta {
    /**
     * The library's old JAR
     */
    private final Path oldJar;
    /**
     * The library's new JAR
     */
    private final Path newJar;
    /**
     * The list of {@link BreakingChange} extracted from japicmp's classes
     */
    private final Collection<BreakingChange> breakingChanges;

    /**
     * @see #fromJApiCmpDelta(Path, Path, List)
     */
    private Delta(Path oldJar, Path newJar, Collection<BreakingChange> breakingChanges) {
        this.oldJar = oldJar;
        this.newJar = newJar;
        this.breakingChanges = breakingChanges;
    }

    /**
     * Builds a delta model from the list of changes extracted by japicmp
     *
     * @param oldJar the library's old JAR
     * @param newJar the library's new JAR
     * @param classes the list of changes extracted using
     *        {@link japicmp.cmp.JarArchiveComparator#compare(japicmp.cmp.JApiCmpArchive, japicmp.cmp.JApiCmpArchive)}
     * @return a corresponding new delta model
     */
    public static Delta fromJApiCmpDelta(Path oldJar, Path newJar, List<JApiClass> classes) {
        Objects.requireNonNull(oldJar);
        Objects.requireNonNull(newJar);
        Objects.requireNonNull(classes);

        Collection<BreakingChange> breakingChanges = new ArrayList<>();

        // We need to create CtReferences to v1 to map japicmp's delta
        // to our own. Building an empty model with the right
        // classpath allows us to create these references.
        CtModel model = SpoonHelpers.buildSpoonModel(null, oldJar);
        CtPackage root = model.getRootPackage();

        // FIXME: Ok, for some reason, @Deprecated methods (and fields? classes?)
        // do not show up in the resulting model. This means that a @Deprecated
        // method that gets removed can't be mapped to the proper CtElement.

        JApiCmpDeltaVisitor.visit(classes, new JApiCmpDeltaVisitor() {
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
        });

        return new Delta(oldJar, newJar, breakingChanges);
    }

    /**
     * Delta models do not natively include source code locations. Invoking
     * this method with the old library's source code populates the source code
     * location for every breaking change.
     *
     * @param sources a {@link java.nio.file.Path} to the old library's source code
     */
    public void populateLocations(Path sources) {
        if (!PathHelpers.isValidDirectory(sources))
            throw new IllegalArgumentException("sources isn't a valid directory");

        CtModel model = SpoonHelpers.buildSpoonModel(sources, null);
        CtPackage root = model.getRootPackage();

        breakingChanges.forEach(bc -> {
            CtReference bytecodeRef = bc.getReference();

            // FIXME: constructor.getDeclaration() always returns null with
            // signature implementation
            if (bytecodeRef == null || bytecodeRef.getDeclaration() == null)
                return;

            if (bytecodeRef instanceof CtTypeReference<?> typeRef) {
                CtTypeReference<?> sourceRef = root.getFactory().Type().createReference(typeRef.getTypeDeclaration());
                bc.setSourceElement(sourceRef.getTypeDeclaration());
            } else if (bytecodeRef instanceof CtExecutableReference<?> execRef) {
                CtExecutableReference<?> sourceRef = root.getFactory().Executable().createReference(execRef.getExecutableDeclaration());
                bc.setSourceElement(sourceRef.getExecutableDeclaration());
            } else if (bytecodeRef instanceof CtFieldReference<?> fieldRef) {
                CtFieldReference<?> sourceRef = root.getFactory().Field().createReference(fieldRef.getFieldDeclaration());
                bc.setSourceElement(sourceRef.getFieldDeclaration());
            } else
                throw new RuntimeException("Shouldn't be here");
        });

        // Remove breaking changes with an implicit source element.
        for (Iterator<BreakingChange> iter = breakingChanges.iterator(); iter.hasNext();) {
            BreakingChange bc = iter.next();
            // FIXME: the isImplicit() method is still not returning the expected
            // output. Using the position as a proxy.
            // if (!SpoonHelpers.isImplicit(bc.getSourceElement()))
            //     iter.remove();
            if (bc.getSourceElement() == null || bc.getSourceElement().getPosition().isValidPosition())
                iter.remove();
        }
    }

    /**
     * Returns a list of {@link com.github.maracas.visitors.BreakingChangeVisitor}, one per {@link com.github.maracas.delta.BreakingChange}
     * in the current delta model. Each visitor is responsible for inferring
     * the set of broken uses in client code impacted by the breaking change.
     */
    public Collection<BreakingChangeVisitor> getVisitors() {
        return
          breakingChanges.stream()
            .map(BreakingChange::getVisitor)
            .filter(Objects::nonNull) // Temporary; FIXME
            .toList();
    }

    /**
     * Returns the list of {@link com.github.maracas.delta.BreakingChange in the current delta model
     */
    public Collection<BreakingChange> getBreakingChanges() {
        return breakingChanges;
    }

    /**
     * Returns the {@link java.nio.file.Path} to the library's old JAR of the current delta
     */
    public Path getOldJar() {
        return oldJar;
    }

    /**
     * Returns the {@link java.nio.file.Path} to the library's new JAR of the current delta
     */
    public Path getNewJar() {
        return newJar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Δ(%s -> %s)\n".formatted(oldJar.getFileName(), newJar.getFileName()));
        sb.append(
          breakingChanges.stream()
            .map(bd -> """
                [%s]
                Reference: %s
                Source: %s %s
                """.formatted(
              bd.getChange(),
              bd.getReference(),
              bd.getSourceElement() instanceof CtNamedElement ne ? ne.getSimpleName() : bd.getSourceElement(),
              bd.getSourceElement() != null ? bd.getSourceElement().getPosition() : "<no source>")
            ).collect(Collectors.joining())
        );
        return sb.toString();
    }
}
