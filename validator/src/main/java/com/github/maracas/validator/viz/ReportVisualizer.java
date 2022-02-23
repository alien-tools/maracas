package com.github.maracas.validator.viz;

import java.nio.file.Path;
import java.util.Collection;

import com.github.maracas.validator.accuracy.AccuracyCase;

public abstract class ReportVisualizer {

    protected Collection<AccuracyCase> cases;

    protected Path path;

    public ReportVisualizer(Collection<AccuracyCase> cases, Path path) {
        this.cases = cases;
        this.path = path;
    }

    public abstract void generate();
}
