package com.github.maracas.forges.clone.git;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GitClonerTest {
	final Path clone = Path.of("./clones");
	Cloner cloner = new GitCloner();

	@BeforeEach
	void setUp() throws IOException {
		FileUtils.deleteDirectory(clone.toFile());
	}

	@AfterEach
	void tearDown() throws IOException {
		FileUtils.deleteDirectory(clone.toFile());
	}

	private String readHEAD(Path clone) {
		try {
			return Files.readAllLines(clone.resolve(".git/HEAD")).get(0);
		} catch (IOException e) {
			return null;
		}
	}

	@Test
	void clone_repository() {
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		cloner.clone(fixtureMain, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("ref: refs/heads/main")));
	}

	@Test
	void clone_repository_branch() {
		Repository fixtureBranch = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "pr-on-modules");
		cloner.clone(fixtureBranch, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("ref: refs/heads/pr-on-modules")));
	}

	@Test
	void clone_repository_timeout() {
		Repository mrc = new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(mrc, clone, 1));
		assertThat(thrown.getMessage(), containsString("timed out"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_repository_invalid() {
		Repository unknown = new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main");
		Exception thrown = assertThrows(CloneException.class, () ->	cloner.clone(unknown, clone));
		assertThat(thrown.getMessage(), containsString("could not read"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_commit_HEAD() {
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		Commit commit = new Commit(fixtureMain, "HEAD");
		cloner.clone(commit, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("15b08c0f6acba8fe369d0076c583fb22311f8524")));
	}

	@Test
	void clone_commit_sha() {
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		Commit commit = new Commit(fixtureMain, "5afad4ed34354d1413f459973183e2610d932750");
		cloner.clone(commit, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("5afad4ed34354d1413f459973183e2610d932750")));
	}

	@Test
	void clone_commit_timeout() {
		Repository mrc = new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main");
		Commit commit = new Commit(mrc, "HEAD");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(commit, clone, 1));
		assertThat(thrown.getMessage(), containsString("timed out"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_commit_invalid_repository() {
		Repository unknown = new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main");
		Commit commit = new Commit(unknown, "5afad4");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(commit, clone));
		assertThat(thrown.getMessage(), containsString("could not read"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_commit_invalid_sha() {
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		Commit commit = new Commit(fixtureMain, "unknown");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(commit, clone));
		assertThat(thrown.getMessage(), containsString("couldn't find remote ref unknown"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_invalid_location() {
		Path readOnly = clone.resolve("read-only");
		readOnly.toFile().mkdirs();
		readOnly.toFile().setReadOnly();
		Path clone = readOnly.resolve("clone");

		Repository fixtureMain = new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(fixtureMain, clone));
		assertThat(thrown.getMessage(), containsString("Permission denied"));
		assertThat(clone.toFile().exists(), is(false));
	}
}