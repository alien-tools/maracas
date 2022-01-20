package com.github.maracas;

import japicmp.config.Options;
import japicmp.model.AccessModifier;
import japicmp.model.JApiCompatibilityChange;

import java.util.HashSet;
import java.util.Set;

public class MaracasOptions {
  private final Options jApiOptions;
  private final Set<JApiCompatibilityChange> excludedBreakingChanges = new HashSet<>();

  private MaracasOptions(Options jApiOptions) {
    this.jApiOptions = jApiOptions;
  }

  public static Options defaultJApiOptions() {
    Options opts = Options.newDefault();
    opts.setAccessModifier(AccessModifier.PACKAGE_PROTECTED);
    opts.setOutputOnlyModifications(true);
    opts.setIgnoreMissingClasses(true);

    return opts;
  }

  public static MaracasOptions newDefault() {
    MaracasOptions opts = new MaracasOptions(defaultJApiOptions());

    // We don't care about source- and binary-compatible changes (except ADA...)
    opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ADDED_TO_PUBLIC_CLASS);
    opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE);

    // We don't care about super-type changes as they're just redundant
    opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_REMOVED_IN_SUPERCLASS);
    opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE_THAN_IN_SUPERCLASS);
    opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_REMOVED_IN_SUPERCLASS);
    opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_SUPERCLASS);
    opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE);

    return opts;
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
