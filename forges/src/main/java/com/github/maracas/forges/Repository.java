package com.github.maracas.forges;

public record Repository(
  String owner,
  String name,
  String remoteUrl,
  String defaultBranch
) {
  public String fullName() {
    return owner + "/" + name;
  }

  public String buildGitHubFileUrl(String ref, String file, int beginLine, int endLine) {
    return "https://github.com/%s/%s/blob/%s/%s#L%d-L%d".formatted(
      owner,
      name,
      ref,
      file,
      beginLine,
      endLine);
  }
}
