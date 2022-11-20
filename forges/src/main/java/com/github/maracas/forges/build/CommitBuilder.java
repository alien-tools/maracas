package com.github.maracas.forges.build;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Package;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.ClonerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CommitBuilder {
	private final Commit commit;
	private final Path clonePath;
	private final BuildConfig buildConfig;
	private final ClonerFactory clonerFactory;
	private final BuilderFactory builderFactory;

	public CommitBuilder(Commit commit, Path clonePath, BuildConfig buildConfig, ClonerFactory clonerFactory, BuilderFactory builderFactory) {
		Objects.requireNonNull(commit);
		Objects.requireNonNull(clonePath);
		Objects.requireNonNull(buildConfig);
		Objects.requireNonNull(clonerFactory);
		this.commit = commit;
		this.clonePath = clonePath;
		this.buildConfig = buildConfig;
		this.clonerFactory = clonerFactory;
		this.builderFactory = builderFactory;
	}

	public void cloneCommit(int timeoutSeconds) throws CloneException {
		clonerFactory.create(commit.repository()).clone(commit, clonePath, timeoutSeconds);
	}

	public Optional<Path> buildCommit(int timeoutSeconds) throws BuildException {
		Builder builder = builderFactory.create(clonePath, buildConfig);
		builder.build(timeoutSeconds);
		return builder.locateJar();
	}

	public List<Package> locatePackages() {
		return builderFactory.create(clonePath, buildConfig).locatePackages();
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

	@Override
	public String toString() {
		return "CommitBuilder[client=%s, path=%s, config=%s]".formatted(commit, clonePath, buildConfig);
	}
}
