package com.github.maracas.forges;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public record PullRequest(
  Repository repository,
  int number,
  Commit base,
  Commit head
) {
  public String buildGitHubDiffUrl(String file, int line) {
    return "https://github.com/%s/%s/pull/%d/files#diff-%sL%d".formatted(
      repository.owner(),
      repository.name(),
      number,
      Hashing.sha256().hashString(file, StandardCharsets.UTF_8),
      line);
  }
}
