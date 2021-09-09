package org.swat.maracas.rest.tasks;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.swat.maracas.rest.BuildService;
import org.swat.maracas.rest.GithubService;
import org.swat.maracas.rest.breakbot.BreakbotConfig;

/**
 * Clones & builds a repository, returns the JAR
 */
public class CloneAndBuild implements Supplier<Path> {
	private final String url;
	private final String ref;
	private final Path dest;
	private final BreakbotConfig config;

	private final GithubService githubService = new GithubService();
	private final BuildService buildService = new BuildService();

	public CloneAndBuild(String url, String ref, Path dest, BreakbotConfig config) {
		this.url = url;
		this.ref = ref;
		this.dest = dest;
		this.config = config;
	}

	@Override
	public Path get() {
		githubService.cloneRemote(url, ref, dest);
		buildService.build(dest, config);
		return buildService.locateJar(dest, config);
	}
}
