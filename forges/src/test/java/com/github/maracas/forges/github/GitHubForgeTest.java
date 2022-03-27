package com.github.maracas.forges.github;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GitHubForgeTest {
  GitHubForge github;

  @BeforeEach
  void setUp() {
    try {
      GitHub gh = GitHubBuilder.fromEnvironment().build();
      github = new GitHubForge(gh);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void fetchRepository_unknown() {
    assertThrows(ForgeException.class, () -> github.fetchRepository("alien-tools", "unknown"));
  }

  @Test
  void fetchRepository_valid() {
    Repository repo = github.fetchRepository("alien-tools", "maracas");
    assertEquals("alien-tools", repo.owner());
    assertEquals("maracas", repo.name());
    assertEquals("alien-tools/maracas", repo.fullName());
    assertEquals("https://github.com/alien-tools/maracas.git", repo.remoteUrl());
  }

  @Test
  void fetchPullRequest_unknown() {
    Repository repo = github.fetchRepository("alien-tools", "comp-changes");
    assertThrows(ForgeException.class, () -> github.fetchPullRequest(repo, -1));
  }

  @Test
  void fetchPullRequest_valid() {
    Repository repo = github.fetchRepository("alien-tools", "comp-changes");
    PullRequest pr = github.fetchPullRequest(repo, 2);
    assertEquals(2, pr.number());
    assertEquals(new Commit(repo, "00dde47b0bf583c4a9320e2968d5fbad0af81265"), pr.base());
    assertEquals(new Commit(repo, "6c19cc73f549a71f5c8a808f336883d3a7a981f3"), pr.head());
  }
}
