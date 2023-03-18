package com.github.maracas.forges.analysis;

import com.github.maracas.forges.PullRequest;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public record PullRequestAnalysisResult(
    PullRequest pr,
    Map<String, PackageAnalysisResult> packageResults,
    Path basePath
) {
  public PullRequestAnalysisResult {
    Objects.requireNonNull(pr);
    Objects.requireNonNull(packageResults);
    Objects.requireNonNull(basePath);
  }
}
