package org.swat.maracas.spoon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import japicmp.cli.JApiCli.ClassPathMode;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.config.Options;
import japicmp.model.AccessModifier;
import japicmp.model.JApiClass;
import japicmp.output.OutputFilter;
import japicmp.util.Optional;

public class VersionAnalyzer {
	private final Path v1;
	private final Path v2;
	private final List<Path> oldCP = new ArrayList<>();
	private final List<Path> newCP = new ArrayList<>();
	private final Map<Path, Set<Detection>> clients = new HashMap<>();
	private Delta delta;

	public VersionAnalyzer(Path v1, Path v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public void computeDelta() {
		Options defaultOptions = getDefaultOptions();
		JarArchiveComparatorOptions options = JarArchiveComparatorOptions.of(defaultOptions);

		oldCP.forEach(p -> options.getOldClassPath().add(p.toAbsolutePath().toString()));
		newCP.forEach(p -> options.getNewClassPath().add(p.toAbsolutePath().toString()));

		JarArchiveComparator comparator = new JarArchiveComparator(options);

		JApiCmpArchive oldAPI = new JApiCmpArchive(v1.toFile(), "v1");
		JApiCmpArchive newAPI = new JApiCmpArchive(v2.toFile(), "v2");

		List<JApiClass> classes = comparator.compare(oldAPI, newAPI);

		OutputFilter filter = new OutputFilter(defaultOptions);
		filter.filter(classes);

		delta = new Delta(classes);
	}

	public ClientAnalyzer analyzeClient(Path client) {
		ClientAnalyzer analyzer = new ClientAnalyzer(delta, client, v1);
		analyzer.computeDetections();
		clients.put(client, analyzer.getDetections());
		return analyzer;
	}

	public Options getDefaultOptions() {
		Options defaultOptions = Options.newDefault();
		defaultOptions.setAccessModifier(AccessModifier.PROTECTED);
		defaultOptions.setOutputOnlyModifications(true);
		defaultOptions.setClassPathMode(ClassPathMode.TWO_SEPARATE_CLASSPATHS);
		defaultOptions.setIgnoreMissingClasses(false);

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

	public Path getV1() {
		return v1;
	}

	public Path getV2() {
		return v2;
	}

	public Delta getDelta() {
		return delta;
	}

	public Set<Detection> getDetections() {
		return clients.values()
			.stream()
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
	}
}