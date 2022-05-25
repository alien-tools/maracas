package com.github.maracas.experiments.model;

/**
 * Represents a GitHub repository. Includes the fields required for the
 * experiment analysis.
 */
public record Repository(
	String owner,
	String name,
	int stars,
	int mergedPRs,
	boolean maven,
	boolean gradle) {

}
