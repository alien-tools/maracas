package com.github.maracas.forges.analysis;

import com.github.maracas.forges.PullRequest;

import java.util.Map;
import java.util.Objects;

public record PullRequestAnalysisResult(
    PullRequest pr,
    Map<String, PackageAnalysisResult> packageResults
) {
  public PullRequestAnalysisResult {
    Objects.requireNonNull(pr);
    Objects.requireNonNull(packageResults);
  }
}
