package com.github.maracas.compchangestests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.maracas.TestData;
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
	static Set<BrokenUse> brokenUses;
	static Set<BrokenUse> found;

	@BeforeAll
	static void setUp() {
		Path v1 = TestData.compChangesV1;
		Path v2 = TestData.compChangesV2;
		Path client = TestData.compChangesClient;

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
		found = new HashSet<BrokenUse>();
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
