package org.swat.maracas.rest.tasks;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
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

	private static final Logger logger = LogManager.getLogger(CloneAndBuild.class);

	public CloneAndBuild(String url, String ref, Path dest) {
		this.url = url;
		this.ref = ref;
		this.dest = dest;
	}

	@Override
	public Path get() {
		// Re-throw any checked exception as unchecked CloneException/BuildException
		cloneRemote();
		return build();
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

	// FIXME: Will only work with a pom.xml file located in the projet's root
	// producing a JAR in the project's root /target/
	private Path build() throws BuildException {
		Path target = dest.resolve("target");
		Path pom = dest.resolve("pom.xml");

		if (!pom.toFile().exists())
			throw new BuildException("No root pom.xml file in " + dest);

		if (!target.toFile().exists()) {
			logger.info("Building {}", pom);
			Properties properties = new Properties();
			properties.setProperty("skipTests", "true");

		    InvocationRequest request = new DefaultInvocationRequest();
		    request.setPomFile(pom.toFile());
		    request.setGoals(Collections.singletonList("package"));
		    request.setProperties(properties);
		    request.setBatchMode(true);

		    try {
			    Invoker invoker = new DefaultInvoker();
			    InvocationResult result = invoker.execute(request);

			    if (result.getExecutionException() != null)
			    	throw new BuildException("'package' goal failed: " + result.getExecutionException().getMessage());
			    if (result.getExitCode() != 0)
			    	throw new BuildException("'package' goal failed: " + result.getExitCode());
			    if (!target.toFile().exists())
			    	throw new BuildException("'package' goal did not produce a /target/");
		    } catch (MavenInvocationException e) {
		    	throw new BuildException(e);
		    }
		} else logger.info("{} has already been built. Skipping.", dest);

		MavenXpp3Reader reader = new MavenXpp3Reader();
		try (InputStream in = new FileInputStream(pom.toFile())) {
			Model model = reader.read(in);
			String aid = model.getArtifactId();
			String vid = model.getVersion();
			Path jar = target.resolve(String.format("%s-%s.jar", aid, vid));

			if (!jar.toFile().exists())
				throw new BuildException("'package' goal on " + pom + " did not produce a JAR");

			return jar;
		} catch (IOException | XmlPullParserException e) {
			throw new BuildException("pom.xml", e);
		}
	}
}
