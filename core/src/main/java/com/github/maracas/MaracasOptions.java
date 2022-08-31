package com.github.maracas;

import japicmp.config.Options;
import japicmp.model.AccessModifier;
import japicmp.model.JApiCompatibilityChange;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds options to pass to the delta and delta impact analysis, including options
 * that should be passed to JApiCmp ({@link Options}).
 */
public class MaracasOptions {
	private final Options jApiOptions;
	private final Set<JApiCompatibilityChange> excludedBreakingChanges = new HashSet<>();
	private boolean noClasspath;

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

	/**
	 * Default options for Maracas. By default, the following {@link JApiCompatibilityChange} are ignored:
	 * <ul>
	 *   <li>CLASS_NO_LONGER_PUBLIC: subset of CLASS_LESS_ACCESSIBLE</li>
	 *   <li>METHOD_ADDED_TO_PUBLIC_CLASS: compatible change</li>
	 *   <li>METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE: compatible change</li>
	 *   <li>METHOD_REMOVED_IN_SUPERCLASS: super-type changes are redundant</li>
	 *   <li>FIELD_LESS_ACCESSIBLE_THAN_IN_SUPERCLASS: super-type changes are redundant</li>
	 *   <li>FIELD_REMOVED_IN_SUPERCLASS: super-type changes are redundant</li>
	 *   <li>METHOD_ABSTRACT_ADDED_IN_SUPERCLASS: super-type changes are redundant</li>
	 *   <li>METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE: super-type changes are redundant</li>
	 * </ul>
	 *
	 * @return the default options
	 */
	public static MaracasOptions newDefault() {
		MaracasOptions opts = new MaracasOptions(defaultJApiOptions());

		// CLASS_NO_LONGER_PUBLIC is just a subset of CLASS_LESS_ACCESSIBLE
		opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_NO_LONGER_PUBLIC);

		// We don't care about source- and binary-compatible changes (except ADA...)
		opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ADDED_TO_PUBLIC_CLASS);
		opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE);

		// We don't care about super-type changes as they're just redundant
		opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_REMOVED_IN_SUPERCLASS);
		opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE_THAN_IN_SUPERCLASS);
		opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_REMOVED_IN_SUPERCLASS);
		opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_SUPERCLASS);
		opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE);

		// If we can, attempt to build JAR's classpath from the pom.xml they may contain
		opts.noClasspath = false;

		return opts;
	}

	/**
	 * Exclude a breaking change kind ({@link JApiCompatibilityChange}) from the analysis.
	 *
	 * @param c the {@link JApiCompatibilityChange} to ignore
	 */
	public void excludeBreakingChange(JApiCompatibilityChange c) {
		excludedBreakingChanges.add(c);
	}

	/**
	 * If set to true, Maracas won't attempt to find a pom.xml file in the analyzed JAR files
	 * and build a classpath from it.
	 * This makes the analysis less accurate but speeds up the analysis.
	 * Default is false.
	 *
	 * @param noClasspath
	 */
	public void setNoClasspath(boolean noClasspath) {
		this.noClasspath = noClasspath;
	}

	public Set<JApiCompatibilityChange> getExcludedBreakingChanges() {
		return excludedBreakingChanges;
	}

	public boolean isNoClasspath() {
		return noClasspath;
	}

	public Options getJApiOptions() {
		return jApiOptions;
	}
}
