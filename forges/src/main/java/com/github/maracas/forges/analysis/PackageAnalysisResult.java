package com.github.maracas.forges.analysis;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record PackageAnalysisResult(
    String pkdId,
    Delta delta,
    Map<Repository, DeltaImpact> clientResults,
    String error
) {
  public static PackageAnalysisResult success(String pkgId, Delta delta, Map<Repository, DeltaImpact> clientResults) {
    return new PackageAnalysisResult(pkgId, delta, clientResults, null);
  }

  public static PackageAnalysisResult failure(String pkgId, String error) {
    return new PackageAnalysisResult(pkgId, null, Collections.emptyMap(), error);
  }

  public List<BrokenUse> allBrokenUses() {
    return clientResults.values()
      .stream()
      .map(DeltaImpact::getBrokenUses)
      .flatMap(Collection::stream)
      .toList();
  }

  public List<Repository> brokenClients() {
    return clientResults.keySet()
      .stream()
      .filter(c -> !clientResults.get(c).getBrokenUses().isEmpty())
      .toList();
  }
}