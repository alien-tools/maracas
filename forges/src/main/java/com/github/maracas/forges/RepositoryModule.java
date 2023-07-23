package com.github.maracas.forges;

import com.github.maracas.forges.Repository;

public record RepositoryModule(
  Repository repository,
  String id,
  String url
) {

}
