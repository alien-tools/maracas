package com.github.maracas.forges.analysis;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.forges.PullRequest;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

  public List<BreakingChange> breakingChanges() {
    return packageResults().values().stream()
      .map(pkg -> pkg.delta().getBreakingChanges())
      .flatMap(Collection::stream)
      .toList();
  }

  public Set<BrokenUse> brokenUses() {
    return packageResults().values().stream()
      .map(PackageAnalysisResult::allBrokenUses)
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());
  }
}
