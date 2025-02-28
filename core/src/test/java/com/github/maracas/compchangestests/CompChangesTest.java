package com.github.maracas.compchangestests;

import com.github.maracas.*;
import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.util.SpoonHelpers;
import com.google.common.base.Objects;
import japicmp.model.JApiCompatibilityChange;
import org.junit.jupiter.api.BeforeAll;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtNamedElement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CompChangesTest {
	static Set<BrokenUse> brokenUses;

	static {
		LibraryJar v1 = LibraryJar.withSources(TestData.compChangesV1, SourcesDirectory.of(TestData.compChangesSources));
		LibraryJar v2 = LibraryJar.withoutSources(TestData.compChangesV2);
		SourcesDirectory client = SourcesDirectory.of(TestData.compChangesClient);

		// We don't care about proper classpath for these tests
		v1.setNoClasspath(true);

		AnalysisQuery query = AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.client(client)
				.exclude("@main.unstableAnnon.Beta")
				.exclude("@main.unstableAnnon.IsUnstable")
				.exclude("(*.)?unstablePkg(.*)?")
				.build();

		AnalysisResult result = new Maracas().analyze(query);
		brokenUses = result.allBrokenUses();
	}

	public static void assertBrokenUse(String file, int line, String elem, JApiCompatibilityChange change, APIUse use) {
		Optional<BrokenUse> find = findBrokenUse(file, line, elem, change, use);
		assertTrue(
			find.isPresent(),
			String.format("No broken use found in %s:%d [%s] [%s]",
				file, line, change, use)
		);
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

				return line == pos.getLine();
			}).findAny();
	}
}
