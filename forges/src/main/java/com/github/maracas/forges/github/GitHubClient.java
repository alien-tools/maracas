package com.github.maracas.forges.github;

import com.github.maracas.forges.RepositoryModule;

public record GitHubClient(
  String owner,
  String name,
  int stars,
  int forks,
  RepositoryModule dependency
) {

}
