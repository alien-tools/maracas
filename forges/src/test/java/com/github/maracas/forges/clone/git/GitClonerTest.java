package com.github.maracas.forges.clone.git;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GitClonerTest {
	final Path CLONES = Path.of(System.getProperty("java.io.tmpdir")).resolve("clones");
	Cloner cloner = new GitCloner();

	private String readHEAD(Path clone) {
		try {
			return Files.readAllLines(clone.resolve(".git/HEAD")).get(0);
		} catch (IOException e) {
			return null;
		}
	}

	@BeforeEach
	void setUp() throws IOException {
		cloner = new GitCloner();
		FileUtils.deleteDirectory(CLONES.toFile());
	}

	@Test
	void clone_repository() {
		cloner.clone(new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main"), CLONES);
		assertTrue(CLONES.resolve("pom.xml").toFile().exists());
		assertEquals("ref: refs/heads/main", readHEAD(CLONES));
	}

	@Test
	void clone_repository_branch() {
		cloner.clone(new Repository("alien-tools", "comp-changes", "https://github.com/alien-tools/comp-changes", "prepare-v2"), CLONES);
		assertTrue(CLONES.resolve("pom.xml").toFile().exists());
		assertEquals("ref: refs/heads/prepare-v2", readHEAD(CLONES));
	}

	@Test
	void clone_commit() {
		cloner.clone(
			new Commit(
				new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main"),
				"fab7a51c347079dbd40cfe7f9eef81837cf5bfa9"),
			CLONES);
		assertTrue(CLONES.resolve("pom.xml").toFile().exists());
		assertEquals("fab7a51c347079dbd40cfe7f9eef81837cf5bfa9", readHEAD(CLONES));
	}

	@Test
	void clone_repository_invalid() {
		Repository repo = new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main");
		Exception thrown = assertThrows(CloneException.class, () ->
			cloner.clone(repo, CLONES)
		);
		assertThat(thrown.getMessage(), containsString("could not read"));
	}

	@Test
	void clone_commit_invalid_repository() {
		Commit commit = new Commit(
			new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main"),
		"fab7a51c347079dbd40cfe7f9eef81837cf5bfa9");
		Exception thrown = assertThrows(CloneException.class, () ->
			cloner.clone(commit, CLONES)
		);
		assertThat(thrown.getMessage(), containsString("could not read"));
	}

	@Test
	void clone_commit_invalid_sha() {
		Commit commit = new Commit(
			new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main"),
			"unknown");
		Exception thrown = assertThrows(CloneException.class, () ->
			cloner.clone(commit, CLONES)
		);
		assertThat(thrown.getMessage(), containsString("couldn't find remote ref unknown"));
	}

	@Test
	void clone_In_ReadOnly_Location() {
		Path readOnly = CLONES.resolve("read-only");
		readOnly.toFile().mkdirs();
		readOnly.toFile().setReadOnly();
		Path clone = readOnly.resolve("clone");

		Repository repo = new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main");
		Exception thrown = assertThrows(CloneException.class, () ->
			cloner.clone(repo, clone)
		);
		assertThat(thrown.getMessage(), containsString("Permission denied"));
	}
}