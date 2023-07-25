package com.github.maracas.forges.github;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubForgeTest {
  @Mock
  GitHub gh;
  GitHubForge forge;

  @BeforeEach
  void setUp() {
    forge = new GitHubForge(gh);
  }

  @Test
  void fetchRepository_shouldSucceed() throws IOException {
    GHRepository repoFixture = repositoryFixture();
    when(repoFixture.getDefaultBranch()).thenReturn("aBranch");
    when(gh.getRepository("owner/name")).thenReturn(repoFixture);

    Repository repo = forge.fetchRepository("owner", "name");

    assertThat(repo, is(notNullValue()));
    assertThat(repo.owner(), is(equalTo("anOwner")));
    assertThat(repo.name(), is(equalTo("aName")));
    assertThat(repo.remoteUrl(), is(equalTo("aUrl")));
    assertThat(repo.branch(), is(equalTo("aBranch")));
    verify(gh).getRepository("owner/name");
  }

  @Test
  void fetchRepository_shouldFail_whenGitHubFails() throws IOException {
    when(gh.getRepository("owner/name")).thenThrow(new IOException("nope"));

    Exception thrown = assertThrows(ForgeException.class, () -> forge.fetchRepository("owner", "name"));

    assertThat(thrown.getMessage(), is(equalTo("Couldn't fetch repository owner/name")));
    assertThat(thrown.getCause().getMessage(), is(equalTo("nope")));
    verify(gh).getRepository("owner/name");
  }

  @Test
  void fetchRepository_withValidBranch_shouldSucceed() throws IOException {
    GHRepository repoFixture = repositoryFixture();
    GHBranch anotherBranch = mock();
    when(anotherBranch.getName()).thenReturn("anotherBranch");
    when(repoFixture.getBranch("anotherBranch")).thenReturn(anotherBranch);
    when(gh.getRepository("owner/name")).thenReturn(repoFixture);

    Repository repo = forge.fetchRepository("owner", "name", "anotherBranch");

    assertThat(repo, is(notNullValue()));
    assertThat(repo.branch(), is(equalTo("anotherBranch")));
    verify(gh).getRepository("owner/name");
    verify(repoFixture).getBranch("anotherBranch");
  }

  @Test
  void fetchRepository_withInvalidBranch_shouldFail() throws IOException {
    GHRepository repoFixture = mock();
    when(repoFixture.getBranch("anotherBranch")).thenThrow(new IOException("nope"));
    when(gh.getRepository("owner/name")).thenReturn(repoFixture);

    Exception thrown = assertThrows(ForgeException.class, () -> forge.fetchRepository("owner", "name", "anotherBranch"));

    assertThat(thrown.getMessage(), is(equalTo("Couldn't fetch repository owner/name on branch anotherBranch")));
    assertThat(thrown.getCause().getMessage(), is(equalTo("nope")));
    verify(gh).getRepository("owner/name");
    verify(repoFixture).getBranch("anotherBranch");
  }

  @Test
  void fetchPullRequest_shouldSucceed() throws IOException {
    GHRepository repoFixture = repositoryFixture();
    GHPullRequest prFixture = prFixture(repoFixture);
    when(repoFixture.getPullRequest(1)).thenReturn(prFixture);
    when(gh.getRepository("anOwner/aName")).thenReturn(repoFixture);

    PullRequest pr = forge.fetchPullRequest("anOwner", "aName", 1);

    assertThat(pr, is(notNullValue()));
    assertThat(pr.repository().owner(), is(equalTo("anOwner")));
    assertThat(pr.repository().name(), is(equalTo("aName")));
    assertThat(pr.base().sha(), is(equalTo("base-sha")));
    assertThat(pr.head().sha(), is(equalTo("head-sha")));
    assertThat(pr.mergeBase().sha(), is(equalTo("merge-base-sha")));
    assertThat(pr.baseBranch(), is(equalTo("base-ref")));
    assertThat(pr.headBranch(), is(equalTo("head-ref")));
    assertThat(pr.changedFiles(), hasSize(1));
    assertThat(pr.changedFiles().get(0).endsWith("pr-file"), is(true));
  }

  @Test
  void fetchPullRequest_shouldFail_whenGitHubFails() throws IOException {
    GHRepository repoFixture = repositoryFixture();
    when(repoFixture.getDefaultBranch()).thenReturn("aBranch");
    when(repoFixture.getPullRequest(1)).thenThrow(new IOException("nope"));
    when(gh.getRepository("anOwner/aName")).thenReturn(repoFixture);

    Exception thrown = assertThrows(ForgeException.class, () -> forge.fetchPullRequest("anOwner", "aName", 1));

    assertThat(thrown.getMessage(), is(equalTo("Couldn't fetch PR 1 from repository anOwner/aName")));
    assertThat(thrown.getCause().getMessage(), is(equalTo("nope")));
  }

  @Test
  void fetchCommit_shouldSucceed() throws IOException {
    GHRepository repoFixture = repositoryFixture();
    GHCommit commitFixture = mock();
    when(commitFixture.getSHA1()).thenReturn("aSha");
    when(repoFixture.getDefaultBranch()).thenReturn("aBranch");
    when(gh.getRepository("anOwner/aName")).thenReturn(repoFixture);
    when(repoFixture.getCommit("aSha")).thenReturn(commitFixture);

    Commit commit = forge.fetchCommit("anOwner", "aName", "aSha");

    assertThat(commit, is(notNullValue()));
    assertThat(commit.sha(), is(equalTo("aSha")));
  }

  @Test
  void fetchCommit_shouldFail_whenGitHubFails() throws IOException {
    GHRepository repoFixture = repositoryFixture();
    when(repoFixture.getDefaultBranch()).thenReturn("aBranch");
    when(repoFixture.getCommit("aSha")).thenThrow(new IOException("nope"));
    when(gh.getRepository("anOwner/aName")).thenReturn(repoFixture);

    Exception thrown = assertThrows(ForgeException.class, () -> forge.fetchCommit("anOwner", "aName", "aSha"));

    assertThat(thrown.getMessage(), is(equalTo("Couldn't fetch commit aSha from repository anOwner/aName")));
    assertThat(thrown.getCause().getMessage(), is(equalTo("nope")));
  }

  GHRepository repositoryFixture() {
    GHRepository repoFixture = mock();
    when(repoFixture.getOwnerName()).thenReturn("anOwner");
    when(repoFixture.getName()).thenReturn("aName");
    when(repoFixture.getHttpTransportUrl()).thenReturn("aUrl");
    return repoFixture;
  }

  GHPullRequest prFixture(GHRepository repoFixture) throws IOException {
    when(repoFixture.getDefaultBranch()).thenReturn("aBranch");
    GHPullRequest prFixture = mock();
    when(prFixture.getNumber()).thenReturn(1);
    GHCommitPointer base = mock();
    GHCommitPointer head = mock();
    GHCommit baseCommit = mock();
    GHCommit headCommit = mock();
    when(base.getCommit()).thenReturn(baseCommit);
    when(head.getCommit()).thenReturn(headCommit);
    when(base.getSha()).thenReturn("base-sha");
    when(head.getSha()).thenReturn("head-sha");
    when(base.getRef()).thenReturn("base-ref");
    when(head.getRef()).thenReturn("head-ref");
    when(prFixture.getBase()).thenReturn(base);
    when(prFixture.getHead()).thenReturn(head);
    GHCompare compare = mock();
    GHCompare.Commit mergeBase = mock();
    when(mergeBase.getSHA1()).thenReturn("merge-base-sha");
    when(compare.getMergeBaseCommit()).thenReturn(mergeBase);
    when(repoFixture.getCompare(baseCommit, headCommit)).thenReturn(compare);
    GHPullRequestFileDetail prFile = mock();
    when(prFile.getFilename()).thenReturn("pr-file");
    PagedIterable<GHPullRequestFileDetail> prFiles = mock();
    when(prFiles.toList()).thenReturn(List.of(prFile));
    when(prFixture.listFiles()).thenReturn(prFiles);
    return prFixture;
  }
}
