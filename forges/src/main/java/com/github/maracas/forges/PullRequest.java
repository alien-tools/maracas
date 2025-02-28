package com.github.maracas.forges;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/*
 * Consider:
 *   ◯ ---- a ---- b --- c (base)
 *           \
 *            \--- d --- e (head)
 *
 * base       => 'c'
 * head       => 'e'
 * mergeBase  => 'a'
 * baseBranch => 'base'
 * headBranch => 'head'
 */
public record PullRequest(
  Repository repository,
  int number,
  Commit base,
  Commit head,
  Commit mergeBase,
  String baseBranch,
  String headBranch,
  List<Path> changedFiles
) {
  public PullRequest {
    Objects.requireNonNull(repository);
    Objects.requireNonNull(base);
    Objects.requireNonNull(head);
    Objects.requireNonNull(mergeBase);
    Objects.requireNonNull(baseBranch);
    Objects.requireNonNull(headBranch);
    Objects.requireNonNull(changedFiles);
  }

  public String buildGitHubDiffUrl(String file, int line) {
    return "https://github.com/%s/%s/pull/%d/files#diff-%sL%d".formatted(
      repository.owner(),
      repository.name(),
      number,
      Hashing.sha256().hashString(file, StandardCharsets.UTF_8),
      line);
  }

  public String uid() {
    return "%s-%s-%s-%s".formatted(
      repository().owner(),
      repository().name(),
      number(),
      head().shortSha()
    );
  }

  @Override
  public String toString() {
    return "PR#%d [%s/%s] [base=%s, head=%s]".formatted(
      number, repository.owner(), repository.name(), baseBranch, headBranch);
  }
}
