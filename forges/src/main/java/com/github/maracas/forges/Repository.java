package com.github.maracas.forges;

import java.util.Objects;

public record Repository(
  String owner,
  String name,
  String remoteUrl,
  String branch
) {
  public Repository {
    Objects.requireNonNull(owner);
    Objects.requireNonNull(name);
    Objects.requireNonNull(remoteUrl);
    Objects.requireNonNull(branch);
  }

  public String fullName() {
    return "%s/%s".formatted(owner, name);
  }

  public String githubWebUrl() {
    return "https://github.com/%s/%s".formatted(owner, name);
  }

  public String buildGitHubFileUrl(String ref, String file, int beginLine, int endLine) {
    return "https://github.com/%s/%s/blob/%s/%s#L%d-L%d".formatted(
      owner, name, ref, file, beginLine, endLine);
  }

  @Override
  public String toString() {
    return "%s/%s@%s".formatted(owner, name, branch);
  }
}
