package com.github.maracas;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.brokenUse.BrokenUse;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.delta.Delta;
import com.github.maracas.util.PathHelpers;
import com.github.maracas.util.SpoonHelpers;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.CombinedVisitor;
import com.google.common.base.Stopwatch;

import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.JApiClass;
import spoon.reflect.CtModel;

public class Maracas {
	private static final Logger logger = LogManager.getLogger(Maracas.class);

	// Just use the static methods
	private Maracas() {

	}

	/**
	 * Analyzes the given {@code query}
	 *
	 * @param query The query to analyze
	 * @return the resulting {@link AnalysisResult} with delta and broken uses
	 * @throws NullPointerException if query is null
	 */
	public static AnalysisResult analyze(AnalysisQuery query) {
		Objects.requireNonNull(query);

		// Compute the delta model between old and new JARs
		Delta delta = computeDelta(query.getOldJar(), query.getNewJar(), query.getMaracasOptions());

		// If we get the library's sources, populate the delta's source code locations
		if (query.getSources() != null)
			delta.populateLocations(query.getSources());

		// For every client, compute the set of broken uses
		Map<Path, Set<BrokenUse>> clientsBrokenUses = new HashMap<>();
		query.getClients()
			.forEach(c ->
				clientsBrokenUses.put(c, computeBrokenUses(c, delta))
			);

		return new AnalysisResult(delta, clientsBrokenUses);
	}

	/**
	 * Compares the library's old and new JARs and returns a delta model
	 * containing all {@link BreakingChange} between them, based on JApiCmp.
	 *
	 * @param oldJar  the library's old JAR
	 * @param newJar  the library's new JAR
	 * @param options Maracas and JApiCmp options
	 * @return a new delta model based on JapiCmp's results
	 * @see JarArchiveComparator#compare(JApiCmpArchive, JApiCmpArchive)
	 * @see #computeDelta(Path, Path, MaracasOptions)
	 * @throws IllegalArgumentException if oldJar or newJar aren't valid
	 */
	public static Delta computeDelta(Path oldJar, Path newJar, MaracasOptions options) {
		if (!PathHelpers.isValidJar(oldJar))
			throw new IllegalArgumentException("oldJar isn't a valid JAR: " + oldJar);
		if (!PathHelpers.isValidJar(newJar))
			throw new IllegalArgumentException("newJar isn't a valid JAR: " + newJar);

		Stopwatch sw = Stopwatch.createStarted();
		MaracasOptions opts = options != null ? options : MaracasOptions.newDefault();
		JarArchiveComparatorOptions jApiOptions = JarArchiveComparatorOptions.of(opts.getJApiOptions());
		JarArchiveComparator comparator = new JarArchiveComparator(jApiOptions);

		JApiCmpArchive oldAPI = new JApiCmpArchive(oldJar.toFile(), "v1");
		JApiCmpArchive newAPI = new JApiCmpArchive(newJar.toFile(), "v2");

		List<JApiClass> classes = comparator.compare(oldAPI, newAPI);
		Delta delta = Delta.fromJApiCmpDelta(
			oldJar.toAbsolutePath(), newJar.toAbsolutePath(), classes, options);

		logger.info("Δ({}, {}) took {}ms", oldJar, newJar, sw.elapsed().toMillis());
		return delta;
	}

	/**
	 * @see #computeDelta(Path, Path, MaracasOptions)
	 */
	public static Delta computeDelta(Path oldJar, Path newJar) {
		return computeDelta(oldJar, newJar, MaracasOptions.newDefault());
	}

	/**
	 * Computes the impact the {@code delta} model has on {@code client} and
	 * returns the corresponding set of {@link BrokenUse}
	 *
	 * @param client valid path to the client's source code to analyze
	 * @param delta  the delta model
	 * @return the corresponding set of {@link BrokenUse}
	 * @throws NullPointerException if delta is null
	 * @throws IllegalArgumentException if client isn't a valid directory
	 */
	public static Set<BrokenUse> computeBrokenUses(Path client, Delta delta) {
		Objects.requireNonNull(delta);
		if (!PathHelpers.isValidDirectory(client))
			throw new IllegalArgumentException("client isn't a valid directory: " + client);

		Stopwatch sw = Stopwatch.createStarted();
		CtModel model = SpoonHelpers.buildSpoonModel(client, delta.getOldJar());
		logger.info("Building Spoon model from {} took {}ms", client, sw.elapsed().toMillis());

		sw.reset();
		sw.start();
		Collection<BreakingChangeVisitor> visitors = delta.getVisitors();
		CombinedVisitor visitor = new CombinedVisitor(visitors);

		// FIXME: Only way I found to visit CompilationUnits and Imports in the model
		// This is probably not the right way.
		// We still need to visit the root package afterwards.
		visitor.scan(model.getRootPackage().getFactory().CompilationUnit().getMap());
		visitor.scan(model.getRootPackage());

		logger.info("brokenUses({}) took {}ms", client, sw.elapsed().toMillis());
		return visitor.getBrokenUses();
	}
}
