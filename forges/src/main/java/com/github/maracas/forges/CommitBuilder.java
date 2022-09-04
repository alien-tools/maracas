package com.github.maracas.forges;

import com.github.maracas.forges.build.BuildConfig;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.Builder;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class CommitBuilder {
	private final Commit commit;
	private final Path clonePath;
	private final Path module;
	private final BuildConfig buildConfig;

	public CommitBuilder(Commit commit, Path clonePath, Path module) {
		Objects.requireNonNull(commit);
		Objects.requireNonNull(clonePath);
		Objects.requireNonNull(module);
		this.commit = commit;
		this.clonePath = clonePath;
		this.module = module;
		this.buildConfig = null;
	}

	public CommitBuilder(Commit commit, Path clonePath, BuildConfig buildConfig) {
		Objects.requireNonNull(commit);
		Objects.requireNonNull(clonePath);
		Objects.requireNonNull(buildConfig);
		this.commit = commit;
		this.clonePath = clonePath;
		this.module = Path.of("");
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

	public Path getClonePath() {
		return clonePath;
	}

	public Path getModule() {
		return module;
	}

	public Path getModulePath() {
		return clonePath.resolve(module);
	}
}
