package com.github.maracas.forges;

public record Commit(
  Repository repository,
  String sha,
  String branch
) {

}
