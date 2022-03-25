package com.github.maracas.forges.clone.git;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.forges.clone.Cloner;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class GitClonerTest {
  final Path CLONES = Paths.get(System.getProperty("java.io.tmpdir")).resolve("clones");
  Cloner cloner = new GitCloner();

  @BeforeEach
  void setUp() throws IOException {
    cloner = new GitCloner();
    FileUtils.deleteDirectory(CLONES.toFile());
  }

  @Test
  void clone_repository() {
    cloner.clone(new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main"), CLONES);
    assertTrue(CLONES.resolve("pom.xml").toFile().exists());
  }

  @Test
  void clone_commit() {
    cloner.clone(
      new Commit(
        new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main"),
        "fab7a51c347079dbd40cfe7f9eef81837cf5bfa9", "main"),
      CLONES);
    assertTrue(CLONES.resolve("pom.xml").toFile().exists());
  }

  @Test
  void clone_repository_invalid() {
    assertThrows(CloneException.class, () ->
      cloner.clone(new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main"), CLONES)
    );
  }

  @Test
  void clone_commit_invalid_repository() {
    assertThrows(CloneException.class, () ->
      cloner.clone(
        new Commit(
          new Repository("alien-tools", "unknown", "https://github.com/alien-tools/unknown", "main"),
          "fab7a51c347079dbd40cfe7f9eef81837cf5bfa9", "main"),
        CLONES)
    );
  }

  @Test
  void clone_commit_invalid_sha() {
    assertThrows(CloneException.class, () ->
      cloner.clone(
        new Commit(
          new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas", "main"),
          "unknown", "main"),
        CLONES)
    );
  }
}