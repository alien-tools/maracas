package com.github.maracas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.maracas.util.GradleLauncher;
import com.github.maracas.util.ParentLastURLClassLoader;
import com.github.maracas.util.PathHelpers;
import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.support.compiler.SpoonPom;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Library {
	private final Path jar;
	private final Path sources;
	private final String label;
	@JsonIgnore
	private List<String> classpath = null;
	@JsonIgnore
	private CtModel binaryModel = null;
	@JsonIgnore
	private CtModel sourceModel = null;
	@JsonIgnore
	private boolean noClasspath = false;

	private static Path TMP_DIR;
	private static final Logger logger = LogManager.getLogger(Library.class);

	public Library(Path jar) {
		this(jar, null);
	}

	public Library(Path jar, Path sources) {
		if (!PathHelpers.isValidJar(jar))
			throw new IllegalArgumentException("Not a valid JAR: " + jar);
		if (sources != null && !PathHelpers.isValidDirectory(sources))
			throw new IllegalArgumentException("Not a valid source directory: " + sources);

		this.jar = jar;
		this.sources = sources;
		this.label = jar.getFileName().toString();
		try {
			TMP_DIR = Files.createTempDirectory("maracas-tmp");
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public CtModel getBinaryModel() {
		if (binaryModel == null)
			binaryModel = buildSpoonModelFromJar();
		return binaryModel;
	}

	public CtModel getSourceModel() {
		if (sourceModel == null)
			sourceModel = buildSpoonModelFromSources();
		return sourceModel;
	}

	private CtModel buildSpoonModelFromJar() {
		Stopwatch sw = Stopwatch.createStarted();
		Launcher launcher = new Launcher();

		// Spoon will prioritize the JVM's classpath over our own
		// custom classpath in case of conflict. Not what we want,
		// so we use a custom child-first classloader instead.
		// cf. https://github.com/INRIA/spoon/issues/3789
		List<URL> jarDependenciesUrl = new ArrayList<>();
		try {
			jarDependenciesUrl.add(new URL("file:" + jar.toAbsolutePath().toString()));
			for (String dep : getClasspath())
				jarDependenciesUrl.add(new URL("file:" + dep));
		} catch (MalformedURLException e) {
			// Checked exceptions are a blessing, kill me
			logger.error(e);
		}
		ClassLoader cl = new ParentLastURLClassLoader(jarDependenciesUrl.toArray(new URL[0]));
		launcher.getEnvironment().setInputClassLoader(cl);

		CtModel model = launcher.buildModel();
		logger.info("Building Spoon model for {} took {}ms", this, sw.elapsed().toMillis());
		return model;
	}

	private CtModel buildSpoonModelFromSources() {
		if (!hasSources())
			return null;

		Stopwatch sw = Stopwatch.createStarted();
		Launcher launcher;
		if (Files.exists(sources.resolve("pom.xml")))
			launcher = new MavenLauncher(sources.toAbsolutePath().toString(), MavenLauncher.SOURCE_TYPE.APP_SOURCE, new String[0]);
		else if (Files.exists(sources.resolve("build.gradle")))
			launcher = new GradleLauncher(sources);
		else {
			launcher = new Launcher();
			launcher.getEnvironment().setComplianceLevel(11);
			launcher.addInputResource(sources.toAbsolutePath().toString());
		}

		// Ignore missing types/classpath related errors
		launcher.getEnvironment().setNoClasspath(true);
		// Proceed even if we find the same type twice; affects the precision of the result
		launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
		// Ignore files with syntax/JLS violations and proceed
		launcher.getEnvironment().setIgnoreSyntaxErrors(true);

		CtModel model = launcher.buildModel();
		logger.info("Building Spoon model for {} took {}ms", this, sw.elapsed().toMillis());
		return model;
	}

	public Path getJar() {
		return jar;
	}

	public boolean hasSources() {
		return sources != null;
	}

	public Path getSources() {
		return sources;
	}

	public String getLabel() {
		return label;
	}

	public List<String> getClasspath() {
		if (noClasspath)
			return Collections.emptyList();

		if (classpath == null)
			classpath = buildClasspath();

		return classpath;
	}

	public void setNoClasspath(boolean noClasspath) {
		this.noClasspath = noClasspath;
	}

	/**
	 * Attempts to build a proper classpath for this library.
	 * From the {@code pom.xml} contained in {@link #sources} if we have it,
	 * from the {@code pom.xml} contained in {@link #jar} otherwise.
	 *
	 * @return the list of JARs upon which this library depends
	 */
	private List<String> buildClasspath() {
		Stopwatch sw = Stopwatch.createStarted();
		Path pom =
			sources != null && sources.resolve("pom.xml").toFile().exists()
				? sources.resolve("pom.xml")
				: extractPomFromJar();

		List<String> cp =
			pom != null
				? buildClasspathFromPom(pom)
				: Collections.emptyList();

		if (pom != null)
			logger.info("Extracting classpath from {} [{} entries] took {}ms", pom, cp.size(), sw.elapsed().toMillis());

		return cp;
	}

	private List<String> buildClasspathFromPom(Path pom) {
		try {
			if (pom.toFile().exists()) {
				SpoonPom spoonPom = new SpoonPom(
					pom.toAbsolutePath().toString(),
					MavenLauncher.SOURCE_TYPE.APP_SOURCE,
					null
				);

				return Arrays.asList(spoonPom.buildClassPath(
					null, MavenLauncher.SOURCE_TYPE.APP_SOURCE, null, true
				));
			}
		} catch (IOException | XmlPullParserException e) {
			logger.error(e);
		}

		return Collections.emptyList();
	}

	private Path extractPomFromJar() {
		try(JarFile jarFile = new JarFile(jar.toFile())) {
			List<JarEntry> poms = jarFile.stream().filter(e -> e.getName().endsWith("pom.xml")).toList();

			if (poms.size() == 1) {
				JarEntry pom = poms.get(0);
				InputStream pomStream = jarFile.getInputStream(pom);

				Path out = Files.createTempFile(TMP_DIR,"pom", ".xml");
				Files.copy(pomStream, out, StandardCopyOption.REPLACE_EXISTING);
				return out;
			} else
				logger.warn("Found {} pom.xml files in {}, no classpath inferred", poms.size(), label);
		} catch (IOException e) {
			logger.error(e);
		}

		return null;
	}

	@Override
	public String toString() {
		return String.format("Library %s [jar=%s sources=%s]",
			label, jar, sources);
	}
}
