package org.swat.maracas.rest.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Clones & builds a repository, returns the JAR
 */
public class CloneAndBuild implements Supplier<Path> {
	private final String url;
	private final String ref;
	private final Path dest;
	private final Path mvnPom;
	private final List<String> mvnGoals;
	private final List<String> mvnProperties;
	private final Path jarLocation;

	private static final Logger logger = LogManager.getLogger(CloneAndBuild.class);

	public CloneAndBuild(String url, String ref, Path dest, Path mvnPom,
		List<String> mvnGoals, List<String> mvnProperties, Path jarLocation) {
		this.url = url;
		this.ref = ref;
		this.dest = dest;
		this.mvnPom = mvnPom;
		this.mvnProperties = mvnProperties;
		this.mvnGoals = mvnGoals;
		this.jarLocation = jarLocation;
	}

	public CloneAndBuild(String url, String ref, Path dest) {
		// By default, we'll `mvn package -DskipTests` on the root pom.xml
		// and look for a JAR in target/
		this(url, ref, dest,
			Paths.get("pom.xml"), Collections.singletonList("package"),
			Collections.singletonList("skipTests"), null);
	}

	@Override
	public Path get() {
		// Re-throw any checked exception as unchecked CloneException/BuildException
		cloneRemote();
		build();
		return locateJar();
	}

	private void cloneRemote() throws CloneException {
		if (dest.toFile().exists()) {
			logger.info("{} exists. Skipping.", dest);
			return;
		}

		logger.info("Cloning {} [{}]", url, ref);
		String fullRef = "refs/heads/" + ref; // FIXME
		CloneCommand clone =
			Git.cloneRepository()
				.setURI(url)
				.setBranchesToClone(Collections.singletonList(fullRef))
				.setBranch(fullRef)
				.setDirectory(dest.toFile());

		try (Git g = clone.call()) {
			// Let me please try-with-resource without a variable :(
		} catch (GitAPIException e) {
			// Rethrow unchecked to get past
			// the Supplier interface
			throw new CloneException(e);
		}
	}

	private void build() {
		File pom = dest.resolve(mvnPom).toFile();
		Path target = dest.resolve(mvnPom).getParent().resolve("target");

		System.out.println("build()");
		System.out.println("pom="+pom);
		System.out.println("target="+target);

		if (!pom.exists())
			throw new BuildException("The pom file " + pom + " could not be found in " + dest);

		if (!target.toFile().exists()) {
			logger.info("Building {}", pom);
			Properties properties = new Properties();
			mvnProperties.forEach(p -> properties.put(p, "true"));

	    InvocationRequest request = new DefaultInvocationRequest();
	    request.setPomFile(pom);
	    request.setGoals(mvnGoals);
	    request.setProperties(properties);
	    request.setBatchMode(true);

	    try {
		    Invoker invoker = new DefaultInvoker();
		    InvocationResult result = invoker.execute(request);

		    if (result.getExecutionException() != null)
		    	throw new BuildException(mvnGoals + " failed: " + result.getExecutionException().getMessage());
		    if (result.getExitCode() != 0)
		    	throw new BuildException(mvnGoals + " failed: " + result.getExitCode());
		    if (!target.toFile().exists())
		    	throw new BuildException(mvnGoals + " goal did not produce a /target/");
	    } catch (MavenInvocationException e) {
	    	throw new BuildException(e);
	    }
		} else logger.info("{} has already been built. Skipping.", dest);
	}

	public Path locateJar() {
		if (jarLocation != null) {
			if (jarLocation.toFile().exists())
				return jarLocation;

			throw new BuildException(mvnGoals + " goal did not produce the JAR " + jarLocation);
		}

		// Otherwise just look for the default location
		File pom = dest.resolve(mvnPom).toFile();
		Path target = dest.resolve(mvnPom).getParent().resolve("target");
		MavenXpp3Reader reader = new MavenXpp3Reader();
		try (InputStream in = new FileInputStream(pom)) {
			Model model = reader.read(in);
			String aid = model.getArtifactId();
			String vid = model.getVersion();
			Path jar = target.resolve(String.format("%s-%s.jar", aid, vid));

			if (!jar.toFile().exists())
				throw new BuildException(mvnGoals + " goal did not produce the JAR " + jar);

			return jar;
		} catch (IOException | XmlPullParserException e) {
			throw new BuildException("pom.xml", e);
		}
	}
}
