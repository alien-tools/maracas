package com.github.maracas.accuracy;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.maracas.accuracy.AccuracyCase.AccuracyType;

/**
 * Analyzes accuracy data.
 */
public class AccuracyAnalyzer {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(AccuracyAnalyzer.class);

    /**
     * Collection of {@link AccuracyCase} (i.e., true positives, false positives,
     * false negatives)
     */
    private Collection<AccuracyCase> cases;

    /**
     * Creates an AccuracyAnalyzer instance. It checks if the cases collection
     * is not null.
     *
     * @param cases collection of {@link AccuracyCase} instances
     */
    public AccuracyAnalyzer(Collection<AccuracyCase> cases) {
        assert cases != null;
        this.cases = cases;
    }

    /**
     * Returns the collection of true positive {@link AccuracyCase} instances
     * from the whole cases collection.
     *
     * @return collection of true positive {@link AccuracyCase} instances
     */
    public Collection<AccuracyCase> truePositives() {
        return filterCases(cases, AccuracyType.TRUE_POSITIVE);
    }

    /**
     * Returns the collection of false positive {@link AccuracyCase} instances
     * from the whole cases collection.
     *
     * @return collection of false positive {@link AccuracyCase} instances
     */
    public Collection<AccuracyCase> falsePositives() {
        return filterCases(cases, AccuracyType.FALSE_POSITIVE);
    }

    /**
     * Returns the collection of false negative {@link AccuracyCase} instances
     * from the whole cases collection.
     *
     * @return collection of false negative {@link AccuracyCase} instances
     */
    public Collection<AccuracyCase> falseNegatives() {
        return filterCases(cases, AccuracyType.FALSE_NEGATIVE);
    }

    /**
     * Filters a collection of {@link AccuracyCase} instances based on an
     * {@link AccuracyType}.
     *
     * @param cases collection of {@link AccuracyCase} instances to be filtered
     * @param type {@link AccuracyType} to filter the collection
     * @return filtered collection of {@link AccuracyCase} instances
     */
    private Collection<AccuracyCase> filterCases(Collection<AccuracyCase> cases, AccuracyType type) {
        return cases
            .stream()
            .filter(c -> c.type() == type)
            .collect(Collectors.toList());
    }

    /**
     * Computes the precision of the tool. If the number of positive cases
     * (both true and false cases) is equal to zero, it returns -1.
     *
     * @return precision of the tool
     */
    public float precision() {
        int truePositives = truePositives().size();
        int falsePositives = falsePositives().size();

        if (truePositives + falsePositives <= 0)
            return -1;
        else
            return (float) truePositives / (truePositives + falsePositives);
    }

    /**
     * Computes the recall of the tool. If the number of true positive cases
     * and false negative cases is equal to zero, it returns -1.
     *
     * @return recall of the tool
     */
    public float recall() {
        int truePositives = truePositives().size();
        int falseNegatives = falseNegatives().size();

        if (truePositives + falseNegatives <= 0)
            return -1;
        else
            return (float) truePositives / (truePositives + falseNegatives);
    }
}
