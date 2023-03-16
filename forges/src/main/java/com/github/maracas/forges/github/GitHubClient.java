package com.github.maracas.forges.github;

public record GitHubClient(
  String owner,
  String name,
  int stars,
  int forks,
  GitHubPackage dependency
) {

}
