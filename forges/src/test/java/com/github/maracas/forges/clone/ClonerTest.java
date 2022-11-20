package com.github.maracas.forges.clone;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.git.GitCloner;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

import static org.junit.jupiter.api.Assertions.*;

class ClonerTest {
  final ClonerFactory factory = new ClonerFactory();

  @Test
  void cloner_From_GitHub() {
    Repository gh = new Repository("owner", "name", "https://github.com/owner/name.git", "branch");
    assertThat(factory.create(gh), instanceOf(GitCloner.class));
  }

  @Test
  void cloner_From_Unknown() {
    Repository unknown = new Repository("owner", "name", "remote", "branch");
    assertThrows(CloneException.class, () -> factory.create(unknown));
  }
}