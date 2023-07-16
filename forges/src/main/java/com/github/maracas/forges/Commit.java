package com.github.maracas.forges;

import java.util.Objects;

public record Commit(
  Repository repository,
  String sha
) {
  public Commit {
    Objects.requireNonNull(repository);
    Objects.requireNonNull(sha);
  }

  public String shortSha() {
    return sha.substring(0, Math.min(sha.length(), 7));
  }

  public String uid() {
    return "%s-%s-%s".formatted(repository.owner(), repository.name(), shortSha());
  }

  @Override
  public String toString() {
    return "%s[%s]".formatted(repository, shortSha());
  }
}
