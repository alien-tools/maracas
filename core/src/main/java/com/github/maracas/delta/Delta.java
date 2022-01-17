package com.github.maracas.delta;

import com.github.maracas.util.PathHelpers;
import com.github.maracas.util.SpoonHelpers;
import com.github.maracas.visitors.BreakingChangeVisitor;
import japicmp.model.JApiClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static final Logger logger = LogManager.getLogger(Delta.class);

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

        // We need to create CtReferences to v1 to map japicmp's delta
        // to our own. Building an empty model with the right
        // classpath allows us to create these references.
        CtModel model = SpoonHelpers.buildSpoonModel(null, oldJar);
        CtPackage root = model.getRootPackage();
        JApiCmpToSpoonVisitor visitor = new JApiCmpToSpoonVisitor(root);
        JApiCmpDeltaVisitor.visit(classes, visitor);

        return new Delta(oldJar, newJar, visitor.getBreakingChanges());
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

            if (bytecodeRef instanceof CtTypeReference<?> typeRef && typeRef.getTypeDeclaration() != null) {
                CtTypeReference<?> sourceRef = root.getFactory().Type().createReference(typeRef.getTypeDeclaration());
                bc.setSourceElement(sourceRef.getTypeDeclaration());
            } else if (bytecodeRef instanceof CtExecutableReference<?> execRef && execRef.getExecutableDeclaration() != null) {
                CtTypeReference<?> typeRef = root.getFactory().Type().createReference(execRef.getDeclaringType().getTypeDeclaration());
                Optional<CtExecutableReference<?>> sourceRef =
                  typeRef.getTypeDeclaration().getDeclaredExecutables().stream()
                    .filter(e -> Objects.equals(e.getSignature(), execRef.getSignature()))
                    .findFirst();

                if (sourceRef.isPresent())
                    bc.setSourceElement(sourceRef.get().getExecutableDeclaration());
                else
                    logger.warn("Couldn't resolve method {} in {}: {}", execRef.getSignature(), typeRef, typeRef.getTypeDeclaration().getDeclaredExecutables());
            } else if (bytecodeRef instanceof CtFieldReference<?> fieldRef && fieldRef.getFieldDeclaration() != null) {
                CtTypeReference<?> typeRef = root.getFactory().Type().createReference(fieldRef.getDeclaringType().getTypeDeclaration());
                CtFieldReference<?> sourceRef = typeRef.getTypeDeclaration().getDeclaredField(fieldRef.getSimpleName());
                bc.setSourceElement(sourceRef.getFieldDeclaration());
            } else
                logger.warn("Couldn't resolve source element for {}", bc.getReference());
        });

        // Remove breaking changes with an implicit source element.
        // FIXME: the isImplicit() method is not returning the expected output. Using the position as a proxy.
        for (Iterator<BreakingChange> iter = breakingChanges.iterator(); iter.hasNext();) {
            BreakingChange bc = iter.next();
            // FIXME: the isImplicit() method is still not returning the expected
            // output. Using the position as a proxy.
            // if (!SpoonHelpers.isImplicit(bc.getSourceElement()))
            //     iter.remove();
            if (bc.getSourceElement() == null || !bc.getSourceElement().getPosition().isValidPosition()) {
                logger.warn("Invalid source position for {}", bc.getReference());
                iter.remove();
            } else {
                logger.info("Valid source position for {}", bc.getReference());
            }
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
        return "Δ(%s -> %s)\n".formatted(oldJar.getFileName(), newJar.getFileName()) +
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
            ).collect(Collectors.joining());
    }
}
