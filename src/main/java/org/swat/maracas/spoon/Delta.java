package org.swat.maracas.spoon;

import java.nio.file.Path;
import java.util.List;

import japicmp.cli.JApiCli.ClassPathMode;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.config.Options;
import japicmp.model.AccessModifier;
import japicmp.model.JApiClass;
import japicmp.output.OutputFilter;
import japicmp.util.Optional;

public class Delta {
	public List<JApiClass> compute(Path oldJar, Path newJar, List<Path> oldCP, List<Path> newCP) {
		Options defaultOptions = getDefaultOptions();
		JarArchiveComparatorOptions options = JarArchiveComparatorOptions.of(defaultOptions);

		oldCP.forEach(p -> options.getOldClassPath().add(p.toAbsolutePath().toString()));
		newCP.forEach(p -> options.getNewClassPath().add(p.toAbsolutePath().toString()));

		JarArchiveComparator comparator = new JarArchiveComparator(options);

		JApiCmpArchive oldAPI = new JApiCmpArchive(oldJar.toFile(), "v1");
		JApiCmpArchive newAPI = new JApiCmpArchive(newJar.toFile(), "v2");

		List<JApiClass> classes = comparator.compare(oldAPI, newAPI);

		OutputFilter filter = new OutputFilter(defaultOptions);
		filter.filter(classes);

		return classes;
	}

	private Options getDefaultOptions() {
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
}
