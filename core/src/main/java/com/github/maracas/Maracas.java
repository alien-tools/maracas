package com.github.maracas;

import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.delta.Delta;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.CombinedVisitor;
import com.google.common.base.Stopwatch;
import japicmp.cli.JApiCli;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.JApiClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.SpoonException;
import spoon.reflect.CtModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

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
	 * @throws SpoonException if we cannot build the Spoon model from the old JAR or source directory
	 */
	public static AnalysisResult analyze(AnalysisQuery query) {
		Objects.requireNonNull(query);

		// Compute the delta model between old and new JARs
		Delta delta = computeDelta(query.getOldVersion(), query.getNewVersion(), query.getMaracasOptions());

		// If no breaking change, we can skip the rest and just return that
		if (delta.getBreakingChanges().isEmpty())
			return AnalysisResult.noImpact(delta, query.getClients());

		// If we got the library's sources, populate the delta's source code locations
		if (query.getOldVersion().hasSources())
			delta.populateLocations();

		// Compute the impact for each client and return the result
		return new AnalysisResult(
			delta,
			query.getClients().parallelStream().collect(toMap(
				c -> c,
				c -> computeDeltaImpact(c, delta))
			)
		);
	}

	/**
	 * Compares the library's old and new versions and returns a delta model
	 * containing all {@link BreakingChange} between them, based on JApiCmp.
	 *
	 * @param oldVersion the library's old version
	 * @param newVersion the library's new version
	 * @param options Maracas and JApiCmp options
	 * @return a new delta model based on JapiCmp's results
	 * @throws NullPointerException if oldVersion or newVersion is null
	 * @throws SpoonException if we cannot build the Spoon model from the old version
	 * @see JarArchiveComparator#compare(JApiCmpArchive, JApiCmpArchive)
	 * @see #computeDelta(LibraryJar, LibraryJar, MaracasOptions)
	 */
	public static Delta computeDelta(LibraryJar oldVersion, LibraryJar newVersion, MaracasOptions options) {
		Objects.requireNonNull(oldVersion);
		Objects.requireNonNull(newVersion);

		MaracasOptions opts = options != null ? options : MaracasOptions.newDefault();

		// Pass the old version's classpath to JApiCmp for the analysis
		opts.getJApiOptions().setClassPathMode(JApiCli.ClassPathMode.ONE_COMMON_CLASSPATH);
		JarArchiveComparatorOptions jApiOptions = JarArchiveComparatorOptions.of(opts.getJApiOptions());
		jApiOptions.getClassPathEntries().addAll(oldVersion.getClasspath());
		JarArchiveComparator comparator = new JarArchiveComparator(jApiOptions);

		JApiCmpArchive oldAPI = new JApiCmpArchive(oldVersion.getJar().toFile(), oldVersion.getLabel());
		JApiCmpArchive newAPI = new JApiCmpArchive(newVersion.getJar().toFile(), newVersion.getLabel());

		Stopwatch sw = Stopwatch.createStarted();
		List<JApiClass> classes = comparator.compare(oldAPI, newAPI);
		Delta delta = Delta.fromJApiCmpDelta(oldVersion, newVersion, classes, opts);

		logger.info("Δ({}, {}) took {}ms", oldVersion.getLabel(), newVersion.getLabel(), sw.elapsed().toMillis());
		return delta;
	}

	/**
	 * @see #computeDelta(LibraryJar, LibraryJar, MaracasOptions)
	 */
	public static Delta computeDelta(LibraryJar oldVersion, LibraryJar newVersion) {
		return computeDelta(oldVersion, newVersion, MaracasOptions.newDefault());
	}

	/**
	 * Computes the impact the {@code delta} model has on {@code client} and
	 * returns the corresponding {@link DeltaImpact}.
	 *
	 * @param client the client to analyze
	 * @param delta  the delta model
	 * @return the corresponding {@link DeltaImpact}, possibly holding a {@link Throwable} if a problem was encountered
	 * @throws NullPointerException     if client or delta is null
	 * @throws SpoonException           if we cannot build the Spoon model from {@code client}
	 */
	public static DeltaImpact computeDeltaImpact(SourcesDirectory client, Delta delta) {
		Objects.requireNonNull(client);
		Objects.requireNonNull(delta);

		try {
			Stopwatch sw = Stopwatch.createStarted();
			client.setClasspath(Collections.singletonList(delta.getOldVersion().getJar()));
			CtModel model = client.getModel();

			Collection<BreakingChangeVisitor> visitors = delta.getVisitors();
			CombinedVisitor visitor = new CombinedVisitor(visitors);

			// FIXME: Only way I found to visit CompilationUnits and Imports in the model
			// This is probably not the right way.
			visitor.scan(model.getRootPackage().getFactory().CompilationUnit().getMap());
			// We still need to visit the root package afterwards.
			visitor.scan(model.getRootPackage());

			logger.info("brokenUses({}) took {}ms", client, sw.elapsed().toMillis());
			return new DeltaImpact(client, delta, visitor.getBrokenUses());
		} catch (Throwable t) {
			logger.warn("Error building the delta impact for {}: {}", client, t);
			t.printStackTrace();
			return new DeltaImpact(client, delta, t);
		}
	}
}
