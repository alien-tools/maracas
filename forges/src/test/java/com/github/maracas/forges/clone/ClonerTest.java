package com.github.maracas.forges.clone;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.clone.git.GitCloner;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

import static org.junit.jupiter.api.Assertions.*;

class ClonerTest {
  @Test
  void cloner_From_GitHub() {
    Repository gh = new Repository("owner", "name", "https://github.com/owner/name.git", "branch");
    assertThat(Cloner.of(gh), instanceOf(GitCloner.class));
  }

  @Test
  void cloner_From_Unknown() {
    Repository unknown = new Repository("owner", "name", "remote", "branch");
    assertThrows(CloneException.class, () -> Cloner.of(unknown));
  }
}
