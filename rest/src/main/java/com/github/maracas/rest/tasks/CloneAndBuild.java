package com.github.maracas.rest.tasks;

import java.nio.file.Path;
import java.util.function.Supplier;

import com.github.maracas.rest.breakbot.BreakbotConfig;
import com.github.maracas.rest.services.BuildService;
import com.github.maracas.rest.services.GitHubService;

/**
 * Clones &amp; builds a repository, returns the JAR
 */
public class CloneAndBuild implements Supplier<Path> {
	private final String url;
	private final String ref;
	private final Path dest;
	private final BreakbotConfig config;

	private final GitHubService githubService = new GitHubService();
	private final BuildService buildService = new BuildService();

	public CloneAndBuild(String url, String ref, Path dest, BreakbotConfig config) {
		this.url = url;
		this.ref = ref;
		this.dest = dest;
		this.config = config;
	}

	@Override
	public Path get() {
		githubService.cloneRemote(url, ref, null, dest);
		buildService.build(dest, config);
		return buildService.locateJar(dest, config);
	}
}
