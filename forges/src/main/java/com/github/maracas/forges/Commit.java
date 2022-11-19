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

  @Override
  public String toString() {
    return "Commit[%s/%s, sha=%s]".formatted(repository.owner(), repository.name(), sha);
  }
}
