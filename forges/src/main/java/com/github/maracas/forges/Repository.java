package com.github.maracas.forges;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record Repository(
  String owner,
  String name,
  String remoteUrl,
  String branch
) {
  public Repository {
    Objects.requireNonNull(owner);
    Objects.requireNonNull(name);
    Objects.requireNonNull(remoteUrl);
    Objects.requireNonNull(branch);
  }

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

  public String buildGitHubDiffUrl(Commit v1, Commit v2, String file, int beginLine, int endLine) {
    return "https://github.com/%s/%s/compare/%s..%s#diff-%sL%d-L%d".formatted(
      owner,
      name,
      v1.sha(),
      v2.sha(),
      Hashing.sha256().hashString(file, StandardCharsets.UTF_8),
      beginLine,
      endLine
    );
  }

  @Override
  public String toString() {
    return "Repository[%s/%s, branch=%s]".formatted(owner, name, branch);
  }
}
