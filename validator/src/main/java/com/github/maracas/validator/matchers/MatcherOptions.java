package com.github.maracas.validator.matchers;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import japicmp.model.JApiCompatibilityChange;

/**
 * Match options to exclude unsupported breaking changes.
 */
@SuppressWarnings("rawtypes")
public record MatcherOptions(
    Map<Enum, String> excludedBreakingChanges) {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(MatcherOptions.class);

    /**
     * Creates a MatcherOptions instance.
     */
    public MatcherOptions() {
        this(new HashMap<Enum, String>());
    }

    /**
     * Exclude a {@link JApiCompatibilityChange} or {@link MissingJApiCompatibilityChange}
     * instance from the matching process.
     *
     * @param change {@link JApiCompatibilityChange} or {@link MissingJApiCompatibilityChange}
     *               instance to exclude
     * @param regex  RegEx pattern to match the breaking change against a
     *               compiler message
     */
    public void excludeBreakingChange(Enum change, String regex) {
        excludedBreakingChanges.put(change, regex);
    }

    /**
     * Returns a default instance of the class excluding unsupported breaking
     * changes in Maracas core. It shows the current state of Maracas implementation.
     *
     * @return default matcher options
     */
    public static MatcherOptions defaultOptions() {
    	MatcherOptions opts = new MatcherOptions();

        // Coming from Maracas broken uses (unimplemented)
        opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_NO_LONGER_PUBLIC, "classNoLongerPublic");
        opts.excludeBreakingChange(JApiCompatibilityChange.CLASS_TYPE_CHANGED, "classTypeChanged");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_NOW_DEFAULT, "methodAbstractNowDefault");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_LESS_ACCESSIBLE, "methodLessAccessible");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NO_LONGER_STATIC, "methodNoLongerStatic");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NOW_STATIC, "methodNowStatic");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NOW_THROWS_CHECKED_EXCEPTION, "methodNowThrosCheckedException");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NO_LONGER_THROWS_CHECKED_EXCEPTION, "methodNoLongerThrowsCheckedException");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_TO_CLASS, "methodAbstractAddedToClass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_NEW_DEFAULT, "methodNewDefault");
        opts.excludeBreakingChange(JApiCompatibilityChange.CONSTRUCTOR_LESS_ACCESSIBLE, "constructorLessAccessible");

        // Coming from Maracas delta by design
        opts.excludeBreakingChange(JApiCompatibilityChange.SUPERCLASS_MODIFIED_INCOMPATIBLE, "superclassModifiedIncompatible");
        //opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_REMOVED_IN_SUPERCLASS, "methodRemovedInSuperclass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ADDED_TO_PUBLIC_CLASS, "methodAddedToPublicClass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_SUPERCLASS, "methodAbstractAddedInSuperclass");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_ABSTRACT_ADDED_IN_IMPLEMENTED_INTERFACE, "methodAbstractAddedInImplementedInterface");
        opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_DEFAULT_ADDED_IN_IMPLEMENTED_INTERFACE, "methodDefaultAddedInImplementedInterface");
        opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_LESS_ACCESSIBLE_THAN_IN_SUPERCLASS, "fieldLessAccessibleThanInSuperclass");
        //opts.excludeBreakingChange(JApiCompatibilityChange.FIELD_REMOVED_IN_SUPERCLASS, "fieldRemovedInSuperclass");

        // Missing JApiCmp breaking changes
        opts.excludeBreakingChange(MissingJApiCompatibilityChange.METHOD_MORE_ACCESSIBLE, "methodMoreAccessible");

        return opts;
    }
}
