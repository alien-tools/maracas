package org.swat.maracas.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
import org.springframework.stereotype.Service;
import org.swat.maracas.rest.breakbot.BreakbotConfig;

@Service
public class BuildService {
	private static final Logger logger = LogManager.getLogger(BuildService.class);

	public void build(Path dest, BreakbotConfig config) {
		File pom =
			config.getMvnPom() != null ?
				dest.resolve(config.getMvnPom()).toFile() :
				dest.resolve("pom.xml").toFile();

		Path target =
			config.getMvnPom() != null ?
				dest.resolve(config.getMvnPom()).getParent().resolve("target") :
				dest.resolve("target");

		if (!pom.exists())
			throw new BuildException("The pom file " + pom + " could not be found in " + dest);

		if (!target.toFile().exists()) {
			Properties properties = new Properties();
			config.getMvnProperties().forEach(p -> properties.put(p, "true"));
			properties.put("skipTests", "true");

			List<String> mvnGoals =
				!config.getMvnGoals().isEmpty() ?
					config.getMvnGoals() :
					Collections.singletonList("package");

	    InvocationRequest request = new DefaultInvocationRequest();
	    request.setPomFile(pom);
	    request.setGoals(mvnGoals);
	    request.setProperties(properties);
	    request.setBatchMode(true);

	    try {
	    	logger.info("Building {} with pom={} goals={} properties={}",
	    		dest, pom, mvnGoals, properties);
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

	public Path locateJar(Path dest, BreakbotConfig config) {
		if (config.getJarLocation() != null) {
			Path customLocation = dest.resolve(config.getJarLocation());
			if (customLocation.toFile().exists())
				return customLocation;
			throw new BuildException("Couldn't find the JAR " + customLocation);
		}

		// Otherwise just look for the default location
		File pom =
			config.getMvnPom() != null ?
				dest.resolve(config.getMvnPom()).toFile() :
				dest.resolve("pom.xml").toFile();
		Path target =
			config.getMvnPom() != null ?
				dest.resolve(config.getMvnPom()).getParent().resolve("target") :
				dest.resolve("target");

		MavenXpp3Reader reader = new MavenXpp3Reader();
		try (InputStream in = new FileInputStream(pom)) {
			Model model = reader.read(in);
			String aid = model.getArtifactId();
			String vid = model.getVersion();
			Path jar = target.resolve(String.format("%s-%s.jar", aid, vid));

			if (!jar.toFile().exists())
				throw new BuildException("Couldn't find the JAR " + jar);

			return jar;
		} catch (IOException | XmlPullParserException e) {
			throw new BuildException("pom.xml", e);
		}
	}
}
