package com.github.maracas.delta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.MaracasOptions;
import com.github.maracas.util.BinaryToSourceMapper;
import com.github.maracas.util.PathHelpers;
import com.github.maracas.util.SpoonHelpers;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.google.common.base.Stopwatch;
import japicmp.model.JApiClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.SpoonException;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtReference;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

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
	 * @see #fromJApiCmpDelta(Path, Path, List, MaracasOptions)
	 */
	private Delta(Path oldJar, Path newJar, Collection<BreakingChange> breakingChanges) {
		this.oldJar = oldJar;
		this.newJar = newJar;
		this.breakingChanges = breakingChanges;
	}

	/**
	 * Builds a delta model from the list of changes extracted by japicmp
	 *
	 * @param oldJar  the library's old JAR
	 * @param newJar  the library's new JAR
	 * @param classes the list of changes extracted using
	 *                {@link japicmp.cmp.JarArchiveComparator#compare(japicmp.cmp.JApiCmpArchive, japicmp.cmp.JApiCmpArchive)}
	 * @param options Maracas' options
	 * @throws SpoonException if we cannot build the Spoon model from {@code oldJar}
	 * @return a corresponding new delta model
	 */
	public static Delta fromJApiCmpDelta(Path oldJar, Path newJar, List<JApiClass> classes, MaracasOptions options) {
		Objects.requireNonNull(oldJar);
		Objects.requireNonNull(newJar);
		Objects.requireNonNull(classes);
		Objects.requireNonNull(options);

		JApiCmpDeltaFilter filter = new JApiCmpDeltaFilter(options);
		filter.filter(classes);

		// We need to create CtReferences to oldJar to map japicmp's delta
		// to our own. Building an empty model with the right
		// classpath allows us to create these references.
		Stopwatch sw = Stopwatch.createStarted();
		CtModel model = SpoonHelpers.buildSpoonModelJar(oldJar);
		CtPackage root = model.getRootPackage();
		logger.info("Building Spoon model from {} took {}ms", oldJar.getFileName(), sw.elapsed().toMillis());

		sw.reset();
		sw.start();
		JApiCmpToSpoonVisitor visitor = new JApiCmpToSpoonVisitor(root);
		JApiCmpDeltaVisitor.visit(classes, visitor);
		logger.info("Mapping JApiCmp's breaking changes to Spoon took {}ms", sw.elapsed().toMillis());

		return new Delta(oldJar, newJar, visitor.getBreakingChanges());
	}

	/**
	 * Delta models do not natively include source code locations. Invoking
	 * this method with the old library's source code populates the source code
	 * location for every breaking change.
	 *
	 * @param sources a {@link java.nio.file.Path} to the old library's source code
	 * @throws SpoonException if we cannot build the Spoon model from {@code sources}
	 */
	public void populateLocations(Path sources) {
		if (!PathHelpers.isValidDirectory(sources))
			throw new IllegalArgumentException("sources isn't a valid directory");

		Stopwatch sw = Stopwatch.createStarted();
		CtModel model = SpoonHelpers.buildSpoonModelMaven(sources);
		CtPackage root = model.getRootPackage();
		BinaryToSourceMapper mapper = new BinaryToSourceMapper(root);
		logger.info("Building Spoon model from {} took {}ms", sources, sw.elapsed().toMillis());

		sw.reset();
		sw.start();
		breakingChanges.forEach(bc -> {
			CtReference binaryRef = bc.getReference();
			CtElement source = mapper.resolve(binaryRef);

			if (source != null)
				bc.setSourceElement(source);
			else
				logger.warn("Couldn't resolve a source location for {} in {}", binaryRef, sources);
		});

		// Remove breaking changes that do not map to a source location
		breakingChanges.removeIf(bc -> bc.getSourceElement() == null || !bc.getSourceElement().getPosition().isValidPosition());

		logger.info("Mapping binary breaking changes to source code took {}ms", sw.elapsed().toMillis());
	}

	/**
	 * Returns a list of {@link BreakingChangeVisitor}, one per {@link BreakingChange}
	 * in the current delta model. Each visitor is responsible for inferring
	 * the set of broken uses in client code impacted by this breaking change.
	 */
	@JsonIgnore
	public Collection<BreakingChangeVisitor> getVisitors() {
		return
			breakingChanges.stream()
				.map(BreakingChange::getVisitor)
				.filter(Objects::nonNull) // FIXME: Until every visitor is implemented
				.toList();
	}

	/**
	 * Returns the list of {@link BreakingChange} in the current delta model
	 */
	public Collection<BreakingChange> getBreakingChanges() {
		return breakingChanges;
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

	/**
	 * Returns a JSON representation of the delta.
	 *
	 * @return string with the JSON representation of the object
	 * @throws IOException
	 */
	public String toJson() throws JsonProcessingException {
		return new ObjectMapper()
			.writerWithDefaultPrettyPrinter()
			.writeValueAsString(this);
	}

	@Override
	public String toString() {
		return "Δ(%s -> %s)%n".formatted(oldJar.getFileName(), newJar.getFileName()) +
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
				).collect(joining());
	}
}
