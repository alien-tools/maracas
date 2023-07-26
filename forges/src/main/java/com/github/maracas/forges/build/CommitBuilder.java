package com.github.maracas.forges.build;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class CommitBuilder {
	private final Commit commit;
	private final Path clonePath;
	private final BuildConfig buildConfig;

	public CommitBuilder(Commit commit, BuildConfig buildConfig, Path clonePath) {
		this.commit = Objects.requireNonNull(commit);
		this.buildConfig = Objects.requireNonNull(buildConfig);
		this.clonePath = Objects.requireNonNull(clonePath);
	}

	public CommitBuilder(Commit commit, BuildConfig buildConfig) {
		this(commit, buildConfig, Path.of("clones").resolve(commit.uid()));
	}

	public CommitBuilder(Commit commit) {
		this(commit, BuildConfig.newDefault(), Path.of("clones").resolve(commit.uid()));
	}

	public void cloneCommit(Duration timeout) throws CloneException {
		getCloner().clone(commit, clonePath, timeout);
	}

	public Optional<Path> buildCommit(Duration timeout) throws BuildException {
		Builder builder = getBuilder();
		builder.build(timeout);
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
