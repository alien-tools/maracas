package com.github.maracas;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.delta.Delta;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
class Usage {
	public static void main(String[] args) {
		LibraryJar v1 = LibraryJar.withSources(
			Path.of("test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar"),
			SourcesDirectory.of(Path.of("test-data/comp-changes/old/")));
		LibraryJar v2 = LibraryJar.withoutSources(Path.of("test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar"));
		SourcesDirectory client = SourcesDirectory.of(Path.of("test-data/comp-changes/client/"));

		AnalysisQuery query = AnalysisQuery.builder()
			.oldVersion(v1)
			.newVersion(v2)
			.client(client)
			.build();

		AnalysisResult result = new Maracas().analyze(query);
		System.out.println("Changes: " + result.delta());
		System.out.println("Impact:  " + result.allBrokenUses());
	}

	void readmeUsage1() {
		Maracas maracas = new Maracas();

		// Setting up the library versions and clients
		LibraryJar v1 = LibraryJar.withSources(Path.of("v1.jar"), Path.of("v1-sources/"));
		LibraryJar v2 = LibraryJar.withoutSources(Path.of("v2.jar"));
		SourcesDirectory client = SourcesDirectory.of(Path.of("/path/to/client"));

		// Option 1: using the query/result API
		AnalysisQuery query = AnalysisQuery.builder()
			.oldVersion(v1)
			.newVersion(v2)
			.client(client)
			.build();

		AnalysisResult result = maracas.analyze(query);
		Delta delta = result.delta();
		List<BreakingChange> breakingChanges = delta.getBreakingChanges();
		Set<BrokenUse> brokenUses = result.allBrokenUses();
	}

	void readmeUsage2() {
		Maracas maracas = new Maracas();

		// Setting up the library versions and clients
		LibraryJar v1 = LibraryJar.withSources(Path.of("v1.jar"), Path.of("v1-sources/"));
		LibraryJar v2 = LibraryJar.withoutSources(Path.of("v2.jar"));
		SourcesDirectory client = SourcesDirectory.of(Path.of("/path/to/client"));

		// Option 2: invoking the analyses directly
		Delta delta = maracas.computeDelta(v1, v2);
		Collection<BreakingChange> breakingChanges = delta.getBreakingChanges();

		DeltaImpact deltaImpact = maracas.computeDeltaImpact(client, delta);
		Set<BrokenUse> brokenUses = deltaImpact.brokenUses();
	}
}
