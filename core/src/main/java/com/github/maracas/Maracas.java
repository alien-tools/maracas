package com.github.maracas;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.maracas.delta.Delta;
import com.github.maracas.delta.Detection;
import com.github.maracas.visitors.BreakingChangeVisitor;
import com.github.maracas.visitors.CombinedVisitor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import japicmp.cli.JApiCli.ClassPathMode;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.config.Options;
import japicmp.model.AccessModifier;
import japicmp.model.JApiClass;
import japicmp.output.OutputFilter;
import japicmp.util.Optional;
import spoon.Launcher;
import spoon.reflect.CtModel;

public class Maracas {
	public MaracasResult analyze(MaracasQuery query) {
		Delta delta = computeDelta(query.v1(), query.v2(),
			query.oldClasspath(), query.newClasspath());

		if (query.sources() != null)
			delta.populateLocations(query.sources());

		Multimap<Path, Detection> clientsDetections = ArrayListMultimap.create();
		query.clients()
			.forEach(c -> {
				clientsDetections.putAll(c, computeDetections(c, delta));
			});

		return new MaracasResult(delta, clientsDetections);
	}

	public Delta computeDelta(Path v1, Path v2, List<Path> oldCP, List<Path> newCP) {
		Options defaultOptions = getJApiOptions();
		JarArchiveComparatorOptions options = JarArchiveComparatorOptions.of(defaultOptions);

		oldCP.forEach(p -> options.getOldClassPath().add(p.toAbsolutePath().toString()));
		newCP.forEach(p -> options.getNewClassPath().add(p.toAbsolutePath().toString()));

		JarArchiveComparator comparator = new JarArchiveComparator(options);

		JApiCmpArchive oldAPI = new JApiCmpArchive(v1.toFile(), "v1");
		JApiCmpArchive newAPI = new JApiCmpArchive(v2.toFile(), "v2");

		List<JApiClass> classes = comparator.compare(oldAPI, newAPI);

		OutputFilter filter = new OutputFilter(defaultOptions);
		filter.filter(classes);

		return new Delta(v1, v2, classes);
	}

	public Delta computeDelta(Path v1, Path v2) {
		return computeDelta(v1, v2, new ArrayList<>(), new ArrayList<>());
	}

	public List<Detection> computeDetections(Path client, Delta delta) {
		Launcher launcher = new Launcher();
		launcher.addInputResource(client.toAbsolutePath().toString());
		String[] javaCP = { delta.getV1().toAbsolutePath().toString() };
		launcher.getEnvironment().setSourceClasspath(javaCP);
		CtModel model = launcher.buildModel();

		List<BreakingChangeVisitor> visitors = delta.getVisitors();
		CombinedVisitor visitor = new CombinedVisitor(visitors);

		visitor.scan(model.getRootPackage());

		return visitor.getDetections();
	}

	private Options getJApiOptions() {
		Options defaultOptions = Options.newDefault();
		defaultOptions.setAccessModifier(AccessModifier.PROTECTED);
		defaultOptions.setOutputOnlyModifications(true);
		defaultOptions.setClassPathMode(ClassPathMode.TWO_SEPARATE_CLASSPATHS);
		defaultOptions.setIgnoreMissingClasses(false);

		// FIXME: inherited from maracas-rascal
		String[] excl = { "(*.)?tests(.*)?", "(*.)?test(.*)?",
				"@org.junit.After",
				"@org.junit.AfterClass",
				"@org.junit.Before",
				"@org.junit.BeforeClass",
				"@org.junit.Ignore",
				"@org.junit.Test",
				"@org.junit.runner.RunWith" };

		for (String e : excl) {
			defaultOptions.addExcludeFromArgument(Optional.of(e), false);
		}

		return defaultOptions;
	}
}
