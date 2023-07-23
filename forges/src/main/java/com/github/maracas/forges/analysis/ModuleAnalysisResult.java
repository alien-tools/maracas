package com.github.maracas.forges.analysis;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ModuleAnalysisResult(
    RepositoryModule module,
    Delta delta,
    Map<Repository, DeltaImpact> clientResults,
    Path basePath,
    String error
) {
  public static ModuleAnalysisResult success(RepositoryModule module, Delta delta, Map<Repository, DeltaImpact> clientResults, Path basePath) {
    return new ModuleAnalysisResult(module, delta, clientResults, basePath, null);
  }

  public static ModuleAnalysisResult failure(RepositoryModule module, String error) {
    return new ModuleAnalysisResult(module, null, Collections.emptyMap(), null, error);
  }

  public List<BrokenUse> allBrokenUses() {
    return clientResults.values()
      .stream()
      .map(DeltaImpact::brokenUses)
      .flatMap(Collection::stream)
      .toList();
  }

  public List<Repository> brokenClients() {
    return clientResults.keySet()
      .stream()
      .filter(c -> !clientResults.get(c).brokenUses().isEmpty())
      .toList();
  }
}
