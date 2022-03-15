package com.github.maracas.brokenUse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.delta.Delta;

/**
 * A delta impact model lists the broken uses in a client project after computing
 * the delta model between two releases of a library. Broken uses are represented
 * as a set of {@link BrokenUse} instances.
 */
public class DeltaImpact {
    /**
     * Class logger
     */
    private static final Logger logger = LogManager.getLogger(DeltaImpact.class);

    /**
     * The client project
     */
    private final Path client;

    /**
     * The {@link Delta} model computed between two releases of the library
     */
    private final Delta delta;

    /**
     * The set of {@link BrokenUse} instances
     */
    private final Set<BrokenUse> brokenUses;

    /**
     * Creates a {@link BrokenUse} instance.
     *
     * @param client     the client project
     * @param delta      the {@link Delta} model computed between two releases of a
     *                   library
     * @param brokenUses the set of computed {@link BrokenUse} instances
     */
    public DeltaImpact(Path client, Delta delta, Set<BrokenUse> brokenUses) {
        this.client = client;
        this.delta = delta;
        this.brokenUses = brokenUses;
    }

    /**
     * Returns the path to the client project.
     *
     * @return the path to the client project
     */
    public Path getClient() {
        return client;
    }

    /**
     * Returns the associated {@link Delta} model.
     *
     * @return the {@link Delta} model
     */
    public Delta getDelta() {
        return delta;
    }

    /**
     * Returns the set of {@link BrokenUse} instances.
     *
     * @return set of {@link BrokenUse} instances
     */
    public Set<BrokenUse> getBrokenUses() {
        return brokenUses;
    }

    /**
     * Returns a JSON representation of the object.
     *
     * @return string with the JSON representation of the object
     * @throws IOException
     */
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "ΔImpact(%s -> %s ON %s)%n)".formatted(
            delta.getOldJar(),
            delta.getNewJar(),
            client,
            brokenUses.stream()
                .map(bu -> "%n%s%n".formatted(bu.toString()))
                .collect(Collectors.joining()));
    }
}
