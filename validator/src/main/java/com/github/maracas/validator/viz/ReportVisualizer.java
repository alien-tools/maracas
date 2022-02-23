package com.github.maracas.validator.viz;

import java.nio.file.Path;
import java.util.Collection;

import com.github.maracas.validator.accuracy.AccuracyCase;

/**
 * Report visualizer of accuracy metrics
 */
public abstract class ReportVisualizer {
    /**
     * Collection of accuracy cases
     */
    protected Collection<AccuracyCase> cases;

    /**
     * Path where the report must be generated
     */
    protected Path path;

    /**
     * Creates a ReportVisualizer instance.
     *
     * @param cases collection of {@link AccuracyCase} instances
     * @param path  path where the report must be generated
     */
    public ReportVisualizer(Collection<AccuracyCase> cases, Path path) {
        this.cases = cases;
        this.path = path;
    }

    /**
     * Generates an accuracy report.
     */
    public abstract void generate();
}
