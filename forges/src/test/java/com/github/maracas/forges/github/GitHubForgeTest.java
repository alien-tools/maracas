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
  void fetchPullRequest_opened() {
    Repository repo = github.fetchRepository("alien-tools", "comp-changes");
    PullRequest pr = github.fetchPullRequest(repo, 2);
    assertEquals(2, pr.number());
    assertEquals(new Commit(repo, "43463c9c73933ae0e791dbf8d1d6e152101a4ba9"), pr.head());
    assertEquals(new Commit(repo, "00dde47b0bf583c4a9320e2968d5fbad0af81265"), pr.mergeBase());
    assertEquals("main", pr.baseBranch());
    assertEquals("prepare-v2", pr.headBranch());
    assertEquals("alien-tools", pr.repository().owner());
    assertEquals("comp-changes", pr.repository().name());
    assertEquals("main", pr.repository().branch());
  }

  @Test
  void fetchPullRequest_closed() {
    Repository repo = github.fetchRepository("INRIA", "spoon");
    PullRequest pr = github.fetchPullRequest(repo, 4625);
    assertEquals(4625, pr.number());
    assertEquals(new Commit(repo, "1ef7b095d58ff671b74f5eef7186c96aa573304e"), pr.head());
    assertEquals(new Commit(repo, "6095f1ba4c6eb98ad5e251f9c5d0aaedff3a1637"), pr.mergeBase());
    assertEquals("master", pr.baseBranch());
    assertEquals("regression-resource", pr.headBranch());
    assertEquals("INRIA", pr.repository().owner());
    assertEquals("spoon", pr.repository().name());
    assertEquals("master", pr.repository().branch());
  }

  @Test
  void fetchPullRequest_that_was_synchronized() {
    Repository repo = github.fetchRepository("javaparser", "javaparser");
    PullRequest pr = github.fetchPullRequest(repo, 3320);
    assertEquals(3320, pr.number());
    assertEquals(new Commit(repo, "dfb238b9de62242d433decc118c19db1fbe0dd1d"), pr.head());
    assertEquals(new Commit(repo, "c2531bbaa671c854bf57b8dc3bb679f20190ded3"), pr.mergeBase());
    assertEquals("master", pr.baseBranch());
    assertEquals("support-jimfs", pr.headBranch());
    assertEquals("javaparser", pr.repository().owner());
    assertEquals("javaparser", pr.repository().name());
    assertEquals("master", pr.repository().branch());
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
