package org.swat.maracas.spoon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.swat.maracas.spoon.visitors.BreakingChangeVisitor;
import org.swat.maracas.spoon.visitors.CombinedVisitor;
import org.swat.maracas.spoon.visitors.DeltaVisitor;

import japicmp.cli.JApiCli.ClassPathMode;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.config.Options;
import japicmp.model.AccessModifier;
import japicmp.model.JApiClass;
import japicmp.output.Filter;
import japicmp.output.OutputFilter;
import japicmp.util.Optional;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.declaration.CtElement;

public class Maracas {
	private final Path v1;
	private final Path v2;
	private final Path client;
	private final List<Path> oldCP = new ArrayList<>();
	private final List<Path> newCP = new ArrayList<>();
	private final Launcher launcher = new Launcher();
	private CtModel model;
	private List<JApiClass> delta;
	private Set<Detection> detections;

	public Maracas(Path v1, Path v2, Path client) {
		this.v1 = v1;
		this.v2 = v2;
		this.client = client;
	}

	public List<JApiClass> computeDelta() {
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

		delta = classes;
		return delta;
	}

	public Set<Detection> computeDetections() {
		launcher.addInputResource(client.toAbsolutePath().toString());
		String[] cp = { v1.toAbsolutePath().toString() };
		launcher.getEnvironment().setSourceClasspath(cp);
		model = launcher.buildModel();

		DeltaVisitor deltaVisitor = new DeltaVisitor(model.getRootPackage());
		Filter.filter(delta, deltaVisitor);

		List<BreakingChangeVisitor> visitors = deltaVisitor.getVisitors();
		CombinedVisitor visitor = new CombinedVisitor(visitors);

		visitor.scan(model.getRootPackage());

		detections = visitor.getDetections();
		return detections;
	}

	public void writeAnnotatedClient(Path output) {
		detections.forEach(d -> {
			CtElement anchor = SpoonHelper.firstLocatableParent(d.element());
			String comment = String.format("[%s:%s]", d.change(), d.use());

			if (anchor != null)
				anchor.addComment(model.getRootPackage().getFactory().Code().createComment(comment, CommentType.INLINE));
			else
				System.out.println("Cannot attach comment on " + d);
		});

		launcher.setSourceOutputDirectory(output.toFile());
		launcher.prettyprint();
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
}
