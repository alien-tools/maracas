package com.github.maracas.forges.clone.git;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class GitClonerTest {
	@TempDir
	Path cloneDir;
	final Cloner cloner = new GitCloner();

	private String readHEAD(Path clone) {
		try {
			return Files.readAllLines(clone.resolve(".git/HEAD")).get(0);
		} catch (IOException e) {
			return "not-read";
		}
	}

	@Test
	void clone_repository() {
		Path clone = cloneDir.resolve("tmp");
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		cloner.clone(fixtureMain, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("ref: refs/heads/main")));
	}

	@Test
	void clone_repository_branch() {
		Path clone = cloneDir.resolve("tmp");
		Repository fixtureBranch = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "pr-on-modules");
		cloner.clone(fixtureBranch, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("ref: refs/heads/pr-on-modules")));
	}

	@Test
	void clone_repository_timeout() {
		Path clone = cloneDir.resolve("tmp");
		Repository linux = new Repository("torvalds", "linux", "https://github.com/torvalds/linux", "master");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(linux, clone, Duration.ofSeconds(1)));
		assertThat(thrown.getMessage(), containsString("timed out"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_repository_invalid() {
		Path clone = cloneDir.resolve("tmp");
		Repository unknown = new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main");
		Exception thrown = assertThrows(CloneException.class, () ->	cloner.clone(unknown, clone));
		assertThat(thrown.getMessage(), containsString("could not read"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_repository_branch_invalid() {
		Path clone = cloneDir.resolve("tmp");
		Repository unknownBranch = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "unknown");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(unknownBranch, clone));
		assertThat(thrown.getMessage(), containsString("branch unknown not found"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_repository_invalid_location() {
		Path readOnly = cloneDir.resolve("read-only");
		if (!readOnly.toFile().mkdirs())
			fail();
		if (!readOnly.toFile().setReadOnly())
			fail();
		Path clone = readOnly.resolve("clone");

		Repository fixtureMain = new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(fixtureMain, clone));
		assertThat(thrown.getMessage(), containsString("Couldn't create clone directory"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_repository_timeout_invalid() {
		Path clone = cloneDir.resolve("tmp");
		Repository linux = new Repository("torvalds", "linux", "https://github.com/torvalds/linux", "master");
		assertThrows(IllegalArgumentException.class, () -> cloner.clone(linux, clone, Duration.ZERO));
	}

	@Test
	void clone_commit_HEAD() {
		Path clone = cloneDir.resolve("tmp");
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		Commit commit = new Commit(fixtureMain, "HEAD");
		cloner.clone(commit, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("fb25deb0e1dd827140886fddb74314ef6a61c66c")));
	}

	@Test
	void clone_commit_sha_main() {
		Path clone = cloneDir.resolve("tmp");
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		Commit commit = new Commit(fixtureMain, "5afad4ed34354d1413f459973183e2610d932750");
		cloner.clone(commit, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("5afad4ed34354d1413f459973183e2610d932750")));
	}

	@Test
	void clone_commit_sha_branch() {
		Path clone = cloneDir.resolve("tmp");
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "pr-on-modules");
		Commit commit = new Commit(fixtureMain, "b2208730510e973e42bd3a176db5c5169b17a7bf");
		cloner.clone(commit, clone);
		assertThat(clone.resolve("pom.xml").toFile().exists(), is(true));
		assertThat(readHEAD(clone), is(equalTo("b2208730510e973e42bd3a176db5c5169b17a7bf")));
	}

	@Test
	void clone_commit_timeout() {
		Path clone = cloneDir.resolve("tmp");
		Repository linux = new Repository("torvalds", "linux", "https://github.com/torvalds/linux", "master");
		Commit commit = new Commit(linux, "6b872a5ecece462ba02c8cad1c0203583631db2b");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(commit, clone, Duration.ofSeconds(1)));
		assertThat(thrown.getMessage(), containsString("timed out"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_commit_invalid_repository() {
		Path clone = cloneDir.resolve("tmp");
		Repository unknown = new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main");
		Commit commit = new Commit(unknown, "5afad4ed34354d1413f459973183e2610d932750");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(commit, clone));
		assertThat(thrown.getMessage(), containsString("could not read"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_commit_invalid_sha() {
		Path clone = cloneDir.resolve("tmp");
		Repository fixtureMain = new Repository("alien-tools", "repository-fixture", "https://github.com/alien-tools/repository-fixture", "main");
		Commit commit = new Commit(fixtureMain, "unknown");
		Exception thrown = assertThrows(CloneException.class, () -> cloner.clone(commit, clone));
		assertThat(thrown.getMessage(), containsString("couldn't find remote ref unknown"));
		assertThat(clone.toFile().exists(), is(false));
	}

	@Test
	void clone_commit_timeout_invalid() {
		Path clone = cloneDir.resolve("tmp");
		Repository linux = new Repository("torvalds", "linux", "https://github.com/torvalds/linux", "master");
		Commit commit = new Commit(linux, "6b872a5ecece462ba02c8cad1c0203583631db2b");
		assertThrows(IllegalArgumentException.class, () -> cloner.clone(commit, clone, Duration.ZERO));
	}
}