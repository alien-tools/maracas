package com.github.maracas;

import japicmp.config.Options;
import japicmp.model.AccessModifier;
import japicmp.model.JApiCompatibilityChange;

import java.util.HashSet;
import java.util.Set;

public class MaracasOptions {
  private final Options jApiOptions;
  private final Set<JApiCompatibilityChange> excludedBreakingChanges = new HashSet<>();

  public MaracasOptions(Options jApiOptions) {
    this.jApiOptions = jApiOptions;
  }

  public static MaracasOptions newDefault() {
    return new MaracasOptions(defaultJApiOptions());
  }

  public static Options defaultJApiOptions() {
    Options jApiOptions = Options.newDefault();
    jApiOptions.setAccessModifier(AccessModifier.PROTECTED);
    jApiOptions.setOutputOnlyModifications(true);
    jApiOptions.setIgnoreMissingClasses(true);

    return jApiOptions;
  }

  public void excludeBreakingChange(JApiCompatibilityChange c) {
    excludedBreakingChanges.add(c);
  }

  public Set<JApiCompatibilityChange> getExcludedBreakingChanges() {
    return excludedBreakingChanges;
  }

  public Options getJApiOptions() {
    return jApiOptions;
  }
}
