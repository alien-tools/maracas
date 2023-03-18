package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;

public record GitHubPackage(
  Repository repository,
  String id,
  String url
) {

}
