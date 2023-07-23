package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;

public record GitHubModule(
  Repository repository,
  String id,
  String url
) {

}
