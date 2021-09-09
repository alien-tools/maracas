package com.github.maracas;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.maracas.delta.Delta;
import com.github.maracas.delta.Detection;

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
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class VersionAnalyzer {
	private final Path v1;
	private final Path v2;
	private final List<Path> oldCP = new ArrayList<>();
	private final List<Path> newCP = new ArrayList<>();
	private final Map<Path, ClientAnalyzer> clients = new HashMap<>();
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

		delta = new Delta(v1, v2, classes);
	}

	public void populateLocations(Path sources) {
		if (delta != null) {
			Launcher launcher = new Launcher();
			launcher.addInputResource(sources.toAbsolutePath().toString());
			CtModel model = launcher.buildModel();
			CtPackage root = model.getRootPackage();

			delta.getBrokenDeclarations().forEach(decl -> {
				CtReference bytecodeRef = decl.getReference();
				if (bytecodeRef instanceof CtTypeReference<?> typeRef) {
					CtTypeReference<?> sourceRef = root.getFactory().Type().createReference(typeRef.getTypeDeclaration());
					decl.setSourceElement(sourceRef.getTypeDeclaration());
				} else if (bytecodeRef instanceof CtExecutableReference<?> execRef) {
					// FIXME: hacky; can't get a reference in the same way as the others;
					// 			  won't work with parameters, etc.; FIX
					String signature = String.format("%s %s#%s()", execRef.getType(), execRef.getDeclaringType(), execRef.getSimpleName());
					CtExecutableReference<?> sourceRef = root.getFactory().Executable().createReference(signature);
					decl.setSourceElement(sourceRef.getExecutableDeclaration());
				} else if (bytecodeRef instanceof CtFieldReference<?> fieldRef) {
					CtFieldReference<?> sourceRef = root.getFactory().Field().createReference(fieldRef.getFieldDeclaration());
					decl.setSourceElement(sourceRef.getFieldDeclaration());
				} else
					throw new RuntimeException("Shouldn't be here");
			});
		}
	}

	public ClientAnalyzer analyzeClient(Path client) {
		ClientAnalyzer analyzer = new ClientAnalyzer(delta, client, v1);
		analyzer.computeDetections();
		clients.put(client, analyzer);
		return analyzer;
	}

	public Options getDefaultOptions() {
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
			.map(ClientAnalyzer::getDetections)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
	}
}
