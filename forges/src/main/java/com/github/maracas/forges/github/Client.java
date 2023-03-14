package com.github.maracas.forges.github;

public record Client(
  Package pkg,
  String owner,
  String name,
  int stars,
  int forks
) {

}
