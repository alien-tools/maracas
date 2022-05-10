package com.github.maracas.forges;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class CommitBuilder {
	private final Commit commit;
	private final Path clonePath;
	private Path sources;
	private BuildConfig buildConfig;

	public CommitBuilder(Commit commit, Path clonePath) {
		Objects.requireNonNull(commit);
		Objects.requireNonNull(clonePath);
		this.commit = commit;
		this.clonePath = clonePath;
	}

	public CommitBuilder(Commit commit, Path clonePath, BuildConfig buildConfig) {
		this(commit, clonePath);
		Objects.requireNonNull(buildConfig);

		this.buildConfig = buildConfig;
	}

	public Path cloneCommit() throws CloneException {
		Cloner cloner = Cloner.of(commit.repository());
		return cloner.clone(commit, clonePath);
	}

	public Optional<Path> buildCommit() throws BuildException {
		Builder builder = Builder.of(buildConfig);
		builder.build();
		return builder.locateJar();
	}

	public Optional<Path> cloneAndBuildCommit() throws CloneException, BuildException {
		cloneCommit();
		return buildCommit();
	}

	public Commit getCommit() {
		return this.commit;
	}

	public BuildConfig getBuildConfig() {
		return this.buildConfig;
	}

	public Path getClonePath() {
		return clonePath;
	}
}
