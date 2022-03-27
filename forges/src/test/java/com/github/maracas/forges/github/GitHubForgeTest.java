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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    assertEquals("main", repo.branch());
  }

  @Test
  void fetchRepository_branch_valid() {
    Repository repo = github.fetchRepository("alien-tools", "maracas", "main");
    assertEquals("alien-tools", repo.owner());
    assertEquals("maracas", repo.name());
    assertEquals("alien-tools/maracas", repo.fullName());
    assertEquals("https://github.com/alien-tools/maracas.git", repo.remoteUrl());
    assertEquals("main", repo.branch());
  }

  @Test
  void fetchRepository_branch_unknown() {
    assertThrows(ForgeException.class, () -> github.fetchRepository("alien-tools", "maracas", "unknown"));
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
    assertEquals("main", pr.baseBranch());
    assertEquals("prepare-v2", pr.headBranch());
    assertEquals("alien-tools", pr.repository().owner());
    assertEquals("comp-changes", pr.repository().name());
    assertEquals("alien-tools/comp-changes", pr.repository().fullName());
    assertEquals("https://github.com/alien-tools/comp-changes.git", pr.repository().remoteUrl());
    assertEquals("main", pr.repository().branch());
  }

  @Test
  void fetchCommit_unknown() {
    assertThrows(ForgeException.class, () -> github.fetchCommit("alien-tools", "maracas", "unknown"));
  }

  @Test
  void fetchCommit_valid() {
    Commit c = github.fetchCommit("alien-tools", "maracas", "655f99bad85435c145fc816018962dc7644edb1f");
    assertEquals("655f99bad85435c145fc816018962dc7644edb1f", c.sha());
    assertEquals("alien-tools", c.repository().owner());
    assertEquals("maracas", c.repository().name());
    assertEquals("alien-tools/maracas", c.repository().fullName());
    assertEquals("https://github.com/alien-tools/maracas.git", c.repository().remoteUrl());
    assertEquals("main", c.repository().branch());
  }
}
