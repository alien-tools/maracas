package com.github.maracas.forges.build;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class CommitBuilder {
	private final Commit commit;
	private final Path clonePath;
	private final BuildConfig buildConfig;

	public CommitBuilder(Commit commit, Path clonePath, BuildConfig buildConfig) {
		Objects.requireNonNull(commit);
		Objects.requireNonNull(clonePath);
		Objects.requireNonNull(buildConfig);
		this.commit = commit;
		this.clonePath = clonePath;
		this.buildConfig = buildConfig;
	}

	public CommitBuilder(Commit commit, Path clonePath) {
		this(commit, clonePath, BuildConfig.newDefault());
	}

	public Path cloneCommit(int timeoutSeconds) throws CloneException {
		return getCloner().clone(commit, clonePath, timeoutSeconds);
	}

	public Optional<Path> buildCommit(int timeoutSeconds) throws BuildException {
		Builder builder = getBuilder();
		builder.build(timeoutSeconds);
		return builder.locateJar();
	}

	public Commit getCommit() {
		return this.commit;
	}

	public Path getClonePath() {
		return clonePath;
	}

	public Path getModulePath() {
		return clonePath.resolve(buildConfig.getModule());
	}

	public BuildConfig getBuildConfig() {
		return buildConfig;
	}

	public Cloner getCloner() {
		return Cloner.of(commit.repository());
	}

	public Builder getBuilder() {
		return Builder.of(this);
	}

	@Override
	public String toString() {
		return "CommitBuilder[commit=%s, path=%s, config=%s]".formatted(commit, clonePath, buildConfig);
	}
}
