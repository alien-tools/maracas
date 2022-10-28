package com.github.maracas.delta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.LibraryJar;
import com.github.maracas.MaracasOptions;
import com.github.maracas.util.BinaryToSourceMapper;
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
	 * The old version of the library
	 */
	private final LibraryJar oldVersion;

	/**
	 * The new version of the library
	 */
	private final LibraryJar newVersion;

	/**
	 * The list of {@link BreakingChange} extracted from japicmp's classes
	 */
	private final Collection<BreakingChange> breakingChanges;

	private static final Logger logger = LogManager.getLogger(Delta.class);

	/**
	 * @see #fromJApiCmpDelta(LibraryJar, LibraryJar, List, MaracasOptions)
	 */
	private Delta(LibraryJar oldVersion, LibraryJar newVersion, Collection<BreakingChange> breakingChanges) {
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
		this.breakingChanges = breakingChanges;
	}

	/**
	 * Builds a delta model from the list of changes extracted by japicmp
	 *
	 * @param oldVersion the old version of the library
	 * @param newVersion the new version of the library
	 * @param classes the list of changes extracted using
	 *                {@link japicmp.cmp.JarArchiveComparator#compare(japicmp.cmp.JApiCmpArchive, japicmp.cmp.JApiCmpArchive)}
	 * @param options Maracas' options
	 * @throws SpoonException if we cannot build the Spoon model from {@code oldJar}
	 * @return the corresponding delta model
	 */
	public static Delta fromJApiCmpDelta(LibraryJar oldVersion, LibraryJar newVersion, List<JApiClass> classes, MaracasOptions options) {
		Objects.requireNonNull(oldVersion);
		Objects.requireNonNull(newVersion);
		Objects.requireNonNull(classes);
		Objects.requireNonNull(options);

		// Visit JApi's model to filter out the things we're not interested in
		JApiCmpDeltaFilter filter = new JApiCmpDeltaFilter(options);
		filter.filter(classes);

		CtModel model = oldVersion.getModel();
		CtPackage root = model.getRootPackage();

		// Map the BCs from JApi to Spoon elements
		Stopwatch sw = Stopwatch.createStarted();
		JApiCmpToSpoonVisitor visitor = new JApiCmpToSpoonVisitor(root);
		JApiCmpDeltaVisitor.visit(classes, visitor);
		logger.info("Mapping JApiCmp's breaking changes to Spoon took {}ms", sw.elapsed().toMillis());

		return new Delta(oldVersion, newVersion, visitor.getBreakingChanges());
	}

	/**
	 * Delta models do not natively include source code locations. Invoking
	 * this method with the old library's source code populates the source code
	 * location for most breaking changes.
	 *
	 * @throws SpoonException if we cannot build the Spoon model from {@code sources}
	 */
	public void populateLocations() {
		if (!oldVersion.hasSources())
			return;

		CtModel model = oldVersion.getSources().getModel();
		CtPackage root = model.getRootPackage();

		Stopwatch sw = Stopwatch.createStarted();
		BinaryToSourceMapper mapper = new BinaryToSourceMapper(root);
		breakingChanges.forEach(bc -> {
			try {
				CtReference binaryRef = bc.getReference();
				CtElement source = mapper.resolve(binaryRef);

				if (source != null)
					bc.setSourceElement(source);
				else
					logger.warn("No source location for {} [{}] in {}", binaryRef, bc.getChange(), oldVersion.getSources());
			} catch (NoClassDefFoundError e) {
				logger.error(e);
			}
		});

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
	 * Returns the old {@link LibraryJar}
	 */
	public LibraryJar getOldVersion() {
		return oldVersion;
	}

	/**
	 * Returns the new {@link LibraryJar}
	 */
	public LibraryJar getNewVersion() {
		return newVersion;
	}

	/**
	 * Returns a JSON representation of the delta.
	 *
	 * @return string with the JSON representation of the object
	 * @throws JsonProcessingException if jackson fails
	 */
	public String toJson() throws JsonProcessingException {
		return new ObjectMapper()
			.writerWithDefaultPrettyPrinter()
			.writeValueAsString(this);
	}

	@Override
	public String toString() {
		return "Δ(%s -> %s)%n".formatted(oldVersion.getLabel(), newVersion.getLabel()) +
			breakingChanges.stream()
				.map(bc -> """
					[%s]
					Reference: %s
					Source: %s %s
					""".formatted(
					bc.getChange(),
					bc.getReference(),
					bc.getSourceElement() instanceof CtNamedElement ne ? ne.getSimpleName() : bc.getSourceElement(),
					bc.getSourceElement() != null ? bc.getSourceElement().getPosition() : "<no source>")
				).collect(joining());
	}
}
