package com.github.maracas.delta;

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
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A delta model lists the breaking changes between two versions of a library,
 * represented as a collection of {@link BrokenDeclaration}.
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
     * The list of {@link BrokenDeclaration} extracted from japicmp's classes
     */
    private final Collection<BrokenDeclaration> brokenDeclarations;

    /**
     * @see #fromJApiCmpDelta(Path, Path, List)
     */
    private Delta(Path oldJar, Path newJar, Collection<BrokenDeclaration> decls) {
        this.oldJar = oldJar;
        this.newJar = newJar;
        this.brokenDeclarations = decls;
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

        Collection<BrokenDeclaration> brokenDeclarations = new ArrayList<>();

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
            public void visit(JApiClass jApiClass) {
                CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiClass.getFullyQualifiedName());
                jApiClass.getCompatibilityChanges().forEach(c ->
                    brokenDeclarations.add(new BrokenClass(jApiClass, clsRef, c))
                );

                jApiClass.getInterfaces().forEach(i ->
                    visit(jApiClass, i)
                );
            }

            @Override
            public void visit(JApiMethod jApiMethod) {
                CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiMethod.getjApiClass().getFullyQualifiedName());
                var oldMethodOpt = jApiMethod.getOldMethod();
                var newMethodOpt = jApiMethod.getNewMethod();

                if (oldMethodOpt.isPresent()) {
                    CtMethod oldMethod = oldMethodOpt.get();
                    Optional<CtExecutableReference<?>> mRefOpt =
                        clsRef.getDeclaredExecutables()
                        .stream()
                        .filter(m -> SpoonHelpers.matchingSignatures(m, oldMethod))
                        .findFirst();

                    if (mRefOpt.isPresent()) {
                        jApiMethod.getCompatibilityChanges().forEach(c ->
                            brokenDeclarations.add(new BrokenMethod(jApiMethod, mRefOpt.get(), c))
                        );
                    } else {
                        if (oldMethod.getName().equals("values") || oldMethod.getName().equals("valueOf")) {
                            // When an enum is transformed into anything else,
                            // japicmp reports that valueOf(String)/values() are removed
                            // Ignore. FIXME
                            ;
                        } else {
                            // FIXME: Commenting following lines to avoid verbose log
                            // System.err.println("Spoon's old method cannot be found: " + jApiMethod);
                            // System.err.println("\tKnown bug: is the method @Deprecated?");
                        }
                    }
                } else if (newMethodOpt.isPresent()) {
                    // Added method introducing a breaking change.
                    CtMethod newMethod = newMethodOpt.get();

                    // FIXME: we miss the information about the newly added method
                    if (!(newMethod.getName().equals("values") || newMethod.getName().equals("valueOf"))) {
                        jApiMethod.getCompatibilityChanges().forEach(c ->
                            brokenDeclarations.add(new BrokenClass(jApiMethod.getjApiClass(), clsRef, c))
                        );
                    }

                } else {
                    throw new RuntimeException("The JApiCmp delta model is corrupted.");
                }
            }

            @Override
            public void visit(JApiField jApiField) {
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
            public void visit(JApiConstructor jApiConstructor) {
                CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiConstructor.getjApiClass().getFullyQualifiedName());
                var oldConsOpt = jApiConstructor.getOldConstructor();

                if (oldConsOpt.isPresent()) {
                    CtConstructor oldCons = oldConsOpt.get();
                    Optional<CtExecutableReference<?>> cRefOpt =
                        clsRef.getDeclaredExecutables()
                        .stream()
                        .filter(c -> SpoonHelpers.matchingSignatures(c, oldCons))
                        .findFirst();

                    // TODO: report bug in Spoon. Implicit constructor states that
                    // the opposite when calling isImplicit() method. Using getPosition()
                    // isValid() instead.
                    if (cRefOpt.isPresent()) {
                        jApiConstructor.getCompatibilityChanges().forEach(c ->
                            brokenDeclarations.add(new BrokenMethod(jApiConstructor, cRefOpt.get(), c))
                        );
                    } else {
                        // No old constructor
                    }
                }
            }

            @Override
            public void visit(JApiImplementedInterface jApiImplementedInterface) {
                // Using visit(JApiClass jApiClass, JApiImplementedInterface jApiImplementedInterface)
                // FIXME: is there a way to get the JApiClass from the interface?
            }

            @Override
            public void visit(JApiAnnotation jApiAnnotation) {
            }

            @Override
            public void visit(JApiSuperclass jApiSuperclass) {
                JApiClass jApiClass = jApiSuperclass.getJApiClassOwning();
                CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiClass.getFullyQualifiedName());
                jApiSuperclass.getCompatibilityChanges().forEach(c ->
                    brokenDeclarations.add(new BrokenClass(jApiClass, clsRef, c))
                );
            }

            public void visit(JApiClass jApiClass, JApiImplementedInterface jApiImplementedInterface) {
                CtTypeReference<?> clsRef = root.getFactory().Type().createReference(jApiClass.getFullyQualifiedName());
                jApiImplementedInterface.getCompatibilityChanges().forEach(c ->
                    brokenDeclarations.add(new BrokenClass(jApiClass, clsRef, c))
                );
            }
        });

        return new Delta(oldJar, newJar, brokenDeclarations);
    }

    /**
     * Delta models do not natively include source code locations. Invoking
     * this method with the old library's source code populates the source code
     * location for every breaking change.
     *
     * @param sources a {@link Path} to the old library's source code
     */
    public void populateLocations(Path sources) {
        if (!PathHelpers.isValidDirectory(sources))
            throw new IllegalArgumentException("sources isn't a valid directory");

        Launcher launcher = new Launcher();
        launcher.addInputResource(sources.toAbsolutePath().toString());
        CtModel model = launcher.buildModel();
        CtPackage root = model.getRootPackage();

        brokenDeclarations.forEach(decl -> {
            CtReference bytecodeRef = decl.getReference();
            if (bytecodeRef instanceof CtTypeReference<?> typeRef) {
                // FIXME: Issue with anonymous class in the
                // https://github.com/break-bot/spoon-before-bc/pull/2 example
                if (typeRef.getTypeDeclaration() == null) {
                    System.err.println("Null type for " + typeRef + " [" + decl + "]");
                } else {
                    CtTypeReference<?> sourceRef = root.getFactory().Type().createReference(typeRef.getTypeDeclaration());
                    decl.setSourceElement(sourceRef.getTypeDeclaration());
                }
            } else if (bytecodeRef instanceof CtExecutableReference<?> execRef) {
                CtExecutableReference<?> sourceRef = root.getFactory().Executable().createReference(execRef.getExecutableDeclaration());
                decl.setSourceElement(sourceRef.getExecutableDeclaration());
            } else if (bytecodeRef instanceof CtFieldReference<?> fieldRef) {
                CtFieldReference<?> sourceRef = root.getFactory().Field().createReference(fieldRef.getFieldDeclaration());
                decl.setSourceElement(sourceRef.getFieldDeclaration());
            } else
                throw new RuntimeException("Shouldn't be here");
        });
    }

    /**
     * Returns a list of {@link BreakingChangeVisitor}, one per {@link BrokenDeclaration}
     * in the current delta model. Each visitor is responsible for inferring
     * the set of detections corresponding to the breaking change in client code.
     */
    public Collection<BreakingChangeVisitor> getVisitors() {
        return
            brokenDeclarations.stream()
            .map(BrokenDeclaration::getVisitor)
            .filter(Objects::nonNull) // Temporary; FIXME
            .toList();
    }

    /**
     * Returns the list of {@link BrokenDeclaration in the current delta model
     */
    public Collection<BrokenDeclaration> getBrokenDeclarations() {
        return brokenDeclarations;
    }

    /**
     * Returns the {@link Path} to the library's old JAR of the current delta
     */
    public Path getOldJar() {
        return oldJar;
    }

    /**
     * Returns the {@link Path} to the library's new JAR of the current delta
     */
    public Path getNewJar() {
        return newJar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Δ(%s -> %s)\n".formatted(oldJar.getFileName(), newJar.getFileName()));
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
