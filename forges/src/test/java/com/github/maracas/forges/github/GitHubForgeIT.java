package com.github.maracas.forges.github;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class GitHubForgeIT {
  GitHub gh;
  GitHubForge github;

  @BeforeEach
  void setUp() throws IOException {
    gh = GitHubBuilder.fromEnvironment().build();
    github = new GitHubForge(gh);
  }

  @Test
  void fetchRepository_unknown() {
    assertThrows(ForgeException.class, () -> github.fetchRepository("alien-tools", "unknown"));
  }

  @Test
  void fetchRepository_valid() {
    Repository repo = github.fetchRepository("alien-tools", "repository-fixture");
    assertEquals("alien-tools", repo.owner());
    assertEquals("repository-fixture", repo.name());
    assertEquals("alien-tools/repository-fixture", repo.fullName());
    assertEquals("https://github.com/alien-tools/repository-fixture.git", repo.remoteUrl());
    assertEquals("main", repo.branch());
  }

  @Test
  void fetchRepository_branch_valid() {
    Repository repo = github.fetchRepository("alien-tools", "repository-fixture", "pr-on-a-branch");
    assertEquals("alien-tools", repo.owner());
    assertEquals("repository-fixture", repo.name());
    assertEquals("alien-tools/repository-fixture", repo.fullName());
    assertEquals("https://github.com/alien-tools/repository-fixture.git", repo.remoteUrl());
    assertEquals("pr-on-a-branch", repo.branch());
  }

  @Test
  void fetchRepository_branch_unknown() {
    assertThrows(ForgeException.class, () -> github.fetchRepository("alien-tools", "repository-fixture", "unknown"));
  }

  @Test
  void fetchPullRequest_unknown() {
    Repository repo = github.fetchRepository("alien-tools", "repository-fixture");
    assertThrows(ForgeException.class, () -> github.fetchPullRequest(repo, 999));
  }

  @Test
  void fetchPullRequest_opened() {
    Repository repo = github.fetchRepository("alien-tools", "repository-fixture");
    PullRequest pr = github.fetchPullRequest(repo, 2);
    assertEquals(2, pr.number());
    assertEquals(new Commit(repo, "fde03b35492be04013bc9cb7712ec3ad5d5a9214"), pr.head());
    assertEquals(new Commit(repo, "15b08c0f6acba8fe369d0076c583fb22311f8524"), pr.mergeBase());
    assertEquals("main", pr.baseBranch());
    assertEquals("pr-on-readme", pr.headBranch());
    assertEquals("alien-tools", pr.repository().owner());
    assertEquals("repository-fixture", pr.repository().name());
    assertEquals("main", pr.repository().branch());
    assertEquals(1, pr.changedFiles().size());
  }

  @Test
  void fetchPullRequest_closed() {
    Repository repo = github.fetchRepository("alien-tools", "repository-fixture");
    PullRequest pr = github.fetchPullRequest(repo, 8);
    assertEquals(8, pr.number());
    assertEquals(new Commit(repo, "fde03b35492be04013bc9cb7712ec3ad5d5a9214"), pr.head());
    assertEquals(new Commit(repo, "15b08c0f6acba8fe369d0076c583fb22311f8524"), pr.mergeBase());
    assertEquals("pr-does-not-compile", pr.baseBranch());
    assertEquals("pr-on-readme", pr.headBranch());
    assertEquals("alien-tools", pr.repository().owner());
    assertEquals("repository-fixture", pr.repository().name());
    assertEquals("main", pr.repository().branch());
    assertEquals(1, pr.changedFiles().size());
  }

  @Test
  void fetchPullRequest_that_was_synchronized() {
    Repository repo = github.fetchRepository("alien-tools", "repository-fixture");
    PullRequest pr = github.fetchPullRequest(repo, 1);
    assertEquals(1, pr.number());
    assertEquals(new Commit(repo, "21a00987e142aee347c4df27836fe07909517b29"), pr.head());
    assertEquals(new Commit(repo, "15b08c0f6acba8fe369d0076c583fb22311f8524"), pr.mergeBase());
    assertEquals("main", pr.baseBranch());
    assertEquals("pr-on-modules", pr.headBranch());
    assertEquals("alien-tools", pr.repository().owner());
    assertEquals("repository-fixture", pr.repository().name());
    assertEquals("main", pr.repository().branch());
  }

  @Test
  void fetchCommit_unknown() {
    assertThrows(ForgeException.class, () -> github.fetchCommit("alien-tools", "repository-fixture", "unknown"));
  }

  @Test
  void fetchCommit_valid() {
    Commit c = github.fetchCommit("alien-tools", "repository-fixture", "fec2de87113764cdfeee36c16c84ca3af0d323b9");
    assertEquals("fec2de87113764cdfeee36c16c84ca3af0d323b9", c.sha());
    assertEquals("fec2de8", c.shortSha());
    assertEquals("alien-tools", c.repository().owner());
    assertEquals("repository-fixture", c.repository().name());
    assertEquals("alien-tools/repository-fixture", c.repository().fullName());
    assertEquals("https://github.com/alien-tools/repository-fixture.git", c.repository().remoteUrl());
    assertEquals("main", c.repository().branch());
  }

  @Test
  void fetchCommit_HEAD() {
    Commit c = github.fetchCommit("alien-tools", "repository-fixture", "HEAD");
    assertEquals("fb25deb0e1dd827140886fddb74314ef6a61c66c", c.sha());
    assertEquals("fb25deb", c.shortSha());
    assertEquals("alien-tools", c.repository().owner());
    assertEquals("repository-fixture", c.repository().name());
    assertEquals("alien-tools/repository-fixture", c.repository().fullName());
    assertEquals("https://github.com/alien-tools/repository-fixture.git", c.repository().remoteUrl());
    assertEquals("main", c.repository().branch());
  }

  @Test
  void fetchCommit_short_sha() {
    Commit c = github.fetchCommit("alien-tools", "repository-fixture", "15b08c");
    assertEquals("15b08c0f6acba8fe369d0076c583fb22311f8524", c.sha());
    assertEquals("15b08c0", c.shortSha());
    assertEquals("alien-tools", c.repository().owner());
    assertEquals("repository-fixture", c.repository().name());
    assertEquals("alien-tools/repository-fixture", c.repository().fullName());
    assertEquals("https://github.com/alien-tools/repository-fixture.git", c.repository().remoteUrl());
    assertEquals("main", c.repository().branch());
  }

  @Test
  void fetchTopClients_drill() {
    Repository drill = github.fetchRepository("apache", "drill");
    List<Repository> clients = github.fetchTopStarredClients(new RepositoryModule(drill, "org.apache.drill.exec:drill-rpc", ""), 5, -1);
    assertThat(clients, hasSize(5));
  }

  @Test
  void fetchAllClients_cache() throws IOException {
    Repository repo = github.fetchRepository("alien-tools", "repository-fixture");
    Path cacheDir = Files.createTempDirectory("test-clients-cache");
    File expectedCacheFile = cacheDir.resolve("alien-tools/repository-fixture/module-a-clients.json").toFile();
    github.setClientsCacheDirectory(cacheDir);
    github.setClientsCacheExpirationDays(1);

    assertThat(expectedCacheFile.exists(), is(false));

    List<Repository> clients = github.fetchAllClients(new RepositoryModule(repo, "module-a", ""), -1, -1);
    assertThat(clients, hasSize(2));
    assertThat(expectedCacheFile.exists(), is(true));

    Files.writeString(expectedCacheFile.toPath(), """
      [{ "owner": "alien-tools", "name": "maracas", "stars": 2, "forks": 3 }]""");
    List<Repository> overwrittenClients = github.fetchAllClients(new RepositoryModule(repo, "module-a", ""), 10, -1);
    assertThat(overwrittenClients, hasSize(3));
    assertThat(overwrittenClients, containsInAnyOrder(
      new Repository("alien-tools", "client-fixture-a", "https://github.com/alien-tools/client-fixture-a.git", "main"),
      new Repository("alien-tools", "client-fixture-b", "https://github.com/alien-tools/client-fixture-b.git", "main"),
      new Repository("alien-tools", "maracas", "https://github.com/alien-tools/maracas.git", "main")
    ));

    FileUtils.deleteQuietly(cacheDir.toFile());
  }

  @Test
  void fetchStarredClients_drill() {
    Repository drill = github.fetchRepository("apache", "drill");
    List<Repository> clients = github.fetchTopStarredClients(new RepositoryModule(drill, "org.apache.drill.exec:drill-rpc", ""), -1, 10);
    assertThat(clients, is(not(empty())));
    clients.forEach(client -> {
      try {
        assertThat(gh.getRepository(client.fullName()).getStargazersCount(), is(greaterThanOrEqualTo(10)));
      } catch (IOException e) {
        fail(e);
      }
    });
  }

  @Test
  void fetchTopStarredClients_drill() {
    Repository drill = github.fetchRepository("apache", "drill");
    List<Repository> clients = github.fetchTopStarredClients(new RepositoryModule(drill, "org.apache.drill.exec:drill-rpc", ""), 10, 10);
    assertThat(clients, hasSize(lessThan(10)));
    clients.forEach(client -> {
      try {
        assertThat(gh.getRepository(client.fullName()).getStargazersCount(), is(greaterThanOrEqualTo(10)));
      } catch (IOException e) {
        fail(e);
      }
    });
  }

  @Test
  void fetchClients_unknown_module() {
    Repository drill = github.fetchRepository("apache", "drill");
    List<Repository> clients = github.fetchTopStarredClients(new RepositoryModule(drill, "unknown", ""), 10, -1);
    assertThat(clients, is(empty()));
  }

  @Test
  void fetchAllClients_from_fork() {
    Repository drillFork = github.fetchRepository("break-bot", "drill-fork-for-tests");
    List<Repository> clients = github.fetchAllClients(new RepositoryModule(drillFork, "org.apache.drill.exec:drill-rpc", ""), 10, -1);
    assertThat(clients, hasSize(10));
  }

  @Test
  void fetchAllClients_no_module() {
    Repository ews = github.fetchRepository("OfficeDev", "ews-java-api");
    List<Repository> clients = github.fetchAllClients(new RepositoryModule(ews, "default_module", ""), 10, -1);
    assertThat(clients, hasSize(10));
  }

  @Test
  void fetchCustomClients_fixture() {
    Repository repo = new Repository("alien-tools", "repository-fixture", "", "");
    List<Repository> clients = github.fetchCustomClients(repo);
    assertThat(clients, hasSize(2));
    assertThat(clients, containsInAnyOrder(
      new Repository("alien-tools", "client-fixture-a", "https://github.com/alien-tools/client-fixture-a.git", "main"),
      new Repository("alien-tools", "client-fixture-b", "https://github.com/alien-tools/client-fixture-b.git", "main")
    ));
  }
}
