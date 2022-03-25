package com.github.maracas.forges;

public record PullRequest(
  Repository repository,
  int number,
  Commit base,
  Commit head
) {

}
