package com.github.maracas.forges;

public record Repository(
  String owner,
  String name,
  String remoteUrl
) {
  public String fullName() {
    return owner + "/" + name;
  }
}
