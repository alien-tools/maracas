package com.github.maracas.forges.github;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.Objects;

public class GitHubForge implements Forge {
  private final GitHub gh;

  public GitHubForge(GitHub gh) {
    this.gh = gh;
  }

  @Override
  public Repository fetchRepository(String owner, String name) {
    Objects.requireNonNull(owner);
    Objects.requireNonNull(name);
    String fullName = owner + "/" + name;

    try {
      GHRepository repo = gh.getRepository(fullName);
      return new Repository(owner, name, repo.getHttpTransportUrl(), repo.getDefaultBranch());
    } catch (IOException e) {
      throw new ForgeException("Couldn't fetch repository " + fullName, e);
    }
  }

  @Override
  public Repository fetchRepository(String owner, String name, String branch) {
    Objects.requireNonNull(owner);
    Objects.requireNonNull(name);
    Objects.requireNonNull(branch);
    String fullName = owner + "/" + name;

    try {
      GHRepository repo = gh.getRepository(fullName);
      GHBranch b = repo.getBranch(branch);

      return new Repository(owner, name, repo.getHttpTransportUrl(), branch);
    } catch (IOException e) {
      throw new ForgeException("Couldn't fetch repository %s on branch %s".formatted(fullName, branch), e);
    }
  }

  @Override
  public PullRequest fetchPullRequest(Repository repository, int number) {
    Objects.requireNonNull(repository);

    try {
      GHPullRequest pr = gh.getRepository(repository.fullName()).getPullRequest(number);
      Commit base = new Commit(repository, pr.getBase().getSha());
      Commit head = new Commit(repository, pr.getHead().getSha());

      return new PullRequest(repository, number, base, head);
    } catch (IOException e) {
      throw new ForgeException("Couldn't fetch PR %d from repository %s".formatted(number, repository.fullName()), e);
    }
  }

  @Override
  public Commit fetchCommit(Repository repository, String sha) {
    Objects.requireNonNull(repository);
    Objects.requireNonNull(sha);

    try {
      GHCommit com = gh.getRepository(repository.fullName()).getCommit(sha);

      return new Commit(repository, sha);
    } catch (IOException e) {
      throw new ForgeException("Couldn't fetch commit %s from repository %s".formatted(sha, repository.fullName()), e);
    }
  }
}
