package com.github.maracas.forges.clone;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.git.GitCloner;

import java.util.Objects;

public class ClonerFactory {
	public Cloner create(Repository repository) {
		Objects.requireNonNull(repository);

		if (repository.remoteUrl().startsWith("https://github.com/"))
			return new GitCloner();

		throw new CloneException("Repository scheme " + repository.remoteUrl() + " not supported");
	}
}
