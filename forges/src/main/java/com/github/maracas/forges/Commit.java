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
}
