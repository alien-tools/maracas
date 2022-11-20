package com.github.maracas.forges.build;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.clone.ClonerFactory;

import java.nio.file.Path;
import java.util.Objects;

public class CommitBuilderFactory {
	final ClonerFactory clonerFactory;
	final BuilderFactory builderFactory;

	public CommitBuilderFactory(ClonerFactory clonerFactory, BuilderFactory builderFactory) {
		this.clonerFactory = Objects.requireNonNull(clonerFactory);
		this.builderFactory = Objects.requireNonNull(builderFactory);
	}

	public CommitBuilder createLibraryBuilder(Commit c, Path clonePath, BuildConfig buildConfig) {
		return new CommitBuilder(c, clonePath, buildConfig, clonerFactory, builderFactory);
	}

	public CommitBuilder createClientBuilder(Commit c, Path clonePath, BuildConfig buildConfig) {
		return new CommitBuilder(c, clonePath, buildConfig, clonerFactory, builderFactory);
	}
}
