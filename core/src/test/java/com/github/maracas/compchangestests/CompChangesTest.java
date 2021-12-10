package com.github.maracas.compchangestests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;

import com.github.maracas.AnalysisQuery;
import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.util.SpoonHelpers;
import com.google.common.base.Objects;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtNamedElement;

public class CompChangesTest {
	static Collection<BrokenUse> brokenUses;
	static Collection<BrokenUse> found;

	@BeforeAll
	static void setUp() {
		Path v1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
		Path v2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
		Path client = Paths.get("../test-data/comp-changes/client/src/");

		AnalysisQuery query = AnalysisQuery.builder()
			.oldJar(v1)
			.newJar(v2)
			.client(client)
			.exclude("@main.unstableAnnon.Beta")
			.exclude("@main.unstableAnnon.IsUnstable")
			.exclude("(*.)?unstablePkg(.*)?")
			.build();

		AnalysisResult result = Maracas.analyze(query);
		brokenUses = result.allBrokenUses();
		found = new ArrayList<>();
	}

	public static void assertBrokenUse(String file, int line, String elem, JApiCompatibilityChange change, APIUse use) {
	    Optional<BrokenUse> find = findBrokenUse(file, line, elem, change, use);
        assertTrue(
            find.isPresent(),
            String.format("No broken use found in %s:%d [%s] [%s]",
                file, line, change, use)
            );

        // Store the ones we found
        found.add(find.get());
	}

	public static void assertBrokenUse(String file, int line, JApiCompatibilityChange change, APIUse use) {
		assertBrokenUse(file, line, null, change, use);
	}

	public static void assertNoBrokenUse(String file, int line, String elem, JApiCompatibilityChange change, APIUse use) {
        Optional<BrokenUse> find = findBrokenUse(file, line, elem, change, use);
        assertFalse(
            find.isPresent(),
            String.format("Broken use found in %s:%d [%s] [%s]",
                file, line, change, use)
            );
    }

    public static void assertNoBrokenUse(String file, int line, JApiCompatibilityChange change, APIUse use) {
        assertNoBrokenUse(file, line, null, change, use);
    }

	public static void assertNumberBrokenUses(JApiCompatibilityChange change, int n) {
		List<BrokenUse> ds = brokenUses.stream().filter(d -> d.change() == change).toList();
		List<BrokenUse> extra = ds.stream()
			.filter(d -> !found.contains(d))
			.toList();

		assertEquals(n, ds.size(), ds.toString());
	}

	private static Optional<BrokenUse> findBrokenUse(String file, int line, String elem, JApiCompatibilityChange change, APIUse use) {
        return
            brokenUses.stream().filter(d -> {
                if (change != d.change())
                    return false;
                if (use != d.use())
                    return false;
                if (elem != null) {
                    String elemString =
                        d.element() instanceof CtNamedElement namedElement ?
                            namedElement.getSimpleName() :
                                d.element().toString();
                    if (!Objects.equal(elem, elemString))
                        return false;
                }

                SourcePosition pos = SpoonHelpers.firstLocatableParent(d.element()).getPosition();
                if (pos instanceof NoSourcePosition)
                    return false;
                if (!file.equals(pos.getFile().getName()))
                    return false;
                if (line != pos.getLine())
                    return false;

                return true;
            }).findAny();
    }
}
