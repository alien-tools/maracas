package com.github.maracas.forges.analysis;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.forges.PullRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record PullRequestAnalysisResult(
    PullRequest pr,
    Map<String, ModuleAnalysisResult> moduleResults
) {
  public PullRequestAnalysisResult {
    Objects.requireNonNull(pr);
    Objects.requireNonNull(moduleResults);
  }

  public List<BreakingChange> breakingChanges() {
    return moduleResults().values().stream()
      .map(module -> module.delta().getBreakingChanges())
      .flatMap(Collection::stream)
      .toList();
  }

  public Set<BrokenUse> brokenUses() {
    return moduleResults().values().stream()
      .map(ModuleAnalysisResult::allBrokenUses)
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());
  }
}
