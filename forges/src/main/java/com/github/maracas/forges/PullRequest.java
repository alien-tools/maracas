package com.github.maracas.forges;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/*
 * Consider:
 *   ◯ ---- a ---- b --- c (base)
 *           \
 *            \--- d --- e (head)
 *
 * base   => 'c'
 * head   => 'e'
 * prBase => 'a'
 */
public record PullRequest(
  Repository repository,
  int number,
  Commit base,
  Commit head,
  Commit mergeBase,
  String baseBranch,
  String headBranch
) {
  public PullRequest {
    Objects.requireNonNull(repository);
    Objects.requireNonNull(base);
    Objects.requireNonNull(head);
    Objects.requireNonNull(mergeBase);
    Objects.requireNonNull(baseBranch);
    Objects.requireNonNull(headBranch);
  }

  public String buildGitHubDiffUrl(String file, int line) {
    return "https://github.com/%s/%s/pull/%d/files#diff-%sL%d".formatted(
      repository.owner(),
      repository.name(),
      number,
      Hashing.sha256().hashString(file, StandardCharsets.UTF_8),
      line);
  }
}
