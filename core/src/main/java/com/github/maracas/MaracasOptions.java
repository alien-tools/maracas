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
	private int maxClassLines = Integer.MAX_VALUE;
	private int clientsPerPackage = Integer.MAX_VALUE;
	private int minStarsPerClient = 0;
	private int cloneTimeoutSeconds = Integer.MAX_VALUE;
	private int buildTimeoutSeconds = Integer.MAX_VALUE;

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

	public Set<JApiCompatibilityChange> getExcludedBreakingChanges() {
		return excludedBreakingChanges;
	}

	public void setMaxClassLines(int maxClassLines) {
		if (maxClassLines < 0)
			throw new IllegalArgumentException("maxClassLines < 0");
		this.maxClassLines = maxClassLines;
	}

	public void setClientsPerPackage(int clientsPerPackage) {
		if (clientsPerPackage < 0)
			throw new IllegalArgumentException("clientsPerPackage < 0");
		this.clientsPerPackage = clientsPerPackage;
	}


	public void setMinStarsPerClient(int minStarsPerClient) {
		if (minStarsPerClient < 0)
			throw new IllegalArgumentException("minStarsPerClient < 0");
		this.minStarsPerClient = minStarsPerClient;
	}

	public void setCloneTimeoutSeconds(int cloneTimeoutSeconds) {
		if (cloneTimeoutSeconds < 0)
			throw new IllegalArgumentException("cloneTimeoutSeconds < 0");
		this.cloneTimeoutSeconds = cloneTimeoutSeconds;
	}

	public void setBuildTimeoutSeconds(int buildTimeoutSeconds) {
		if (buildTimeoutSeconds < 0)
			throw new IllegalArgumentException("buildTimeoutSeconds < 0");
		this.buildTimeoutSeconds = buildTimeoutSeconds;
	}

	public int getMaxClassLines() {
		return maxClassLines;
	}

	public int getClientsPerPackage() { return clientsPerPackage; }

	public int getMinStarsPerClient() { return minStarsPerClient; }

	public int getCloneTimeoutSeconds() { return cloneTimeoutSeconds; }

	public int getBuildTimeoutSeconds() { return buildTimeoutSeconds; }

	public Options getJApiOptions() {
		return jApiOptions;
	}
}
