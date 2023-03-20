package com.github.maracas;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;

import japicmp.model.AccessModifier;
import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.position.NoSourcePosition;

class MaracasTest {
	final LibraryJar v1 = LibraryJar.withoutSources(TestData.compChangesV1);
	final LibraryJar v1WithSources = LibraryJar.withSources(TestData.compChangesV1, SourcesDirectory.of(TestData.compChangesSources));
	final LibraryJar v2 = LibraryJar.withoutSources(TestData.compChangesV2);
	final SourcesDirectory client = SourcesDirectory.of(TestData.compChangesClient);
	final SourcesDirectory client2 = SourcesDirectory.of(TestData.compChangesSources);

	@Test
	void analyze_QueryWithoutClient_hasNoBrokenUse() {
		AnalysisResult res = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.build());

		assertThat(res.delta(), is(notNullValue()));
		assertThat(res.delta().getBreakingChanges(),
			everyItem(allOf(
				hasProperty("reference", is(notNullValue())),
				hasProperty("sourceElement", is(nullValue()))
			)));
		assertThat(res.allBrokenUses(), is(empty()));
	}

	@Test
	void analyze_QueryWithTwoClients_hasTwoClientBrokenUses() {
		AnalysisResult res = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.client(client)
				.client(client2)
				.build());

		assertThat(res.delta(), is(notNullValue()));
		assertThat(res.deltaImpacts().keySet(), hasSize(2));
		assertThat(res.deltaImpacts(), hasKey(client.getLocation()));
		assertThat(res.deltaImpacts(), hasKey(client2.getLocation()));
	}

	@Test
	void analyze_QueryWithSources_hasSourceLocations() {
		AnalysisResult res = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1WithSources)
				.newVersion(v2)
				.build());

		assertThat(res.delta(), is(notNullValue()));
		assertThat(res.delta().getBreakingChanges(),
			everyItem(hasProperty("sourceElement", is(notNullValue()))));
	}

	@Test
	void analyze_QueryWithAccessModifier_IsConsidered() {
		MaracasOptions publicOpts = MaracasOptions.newDefault();
		publicOpts.getJApiOptions().setAccessModifier(AccessModifier.PUBLIC);

		MaracasOptions privateOpts = MaracasOptions.newDefault();
		privateOpts.getJApiOptions().setAccessModifier(AccessModifier.PRIVATE);

		AnalysisResult resPublic = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.options(publicOpts)
				.build());

		AnalysisResult resPrivate = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.options(privateOpts)
				.build());

		assertThat(resPublic.delta().getBreakingChanges().size(),
			is(not(equalTo(resPrivate.delta().getBreakingChanges().size()))));
	}

	@Test
	void analyze_QueryWithExcludedBC_IsConsidered() {
		AnalysisResult resWithoutOpts = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.build());

		MaracasOptions opts = MaracasOptions.newDefault();
		opts.excludeBreakingChange(JApiCompatibilityChange.METHOD_REMOVED);
		AnalysisResult resWithOpts = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.options(opts)
				.build());

		assertThat(
			resWithoutOpts.delta().getBreakingChanges().stream()
				.filter(bc -> bc.getChange().equals(JApiCompatibilityChange.METHOD_REMOVED))
				.count(), greaterThan(0L));
		assertThat(
			resWithOpts.delta().getBreakingChanges().stream()
				.filter(bc -> bc.getChange().equals(JApiCompatibilityChange.METHOD_REMOVED))
				.count(), is(equalTo(0L)));
	}

	@Test
	void analyze_QueryWithMaxClassLines_IsConsidered() {
		AnalysisResult resWithoutOpts = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.client(client)
				.build());

		MaracasOptions opts = MaracasOptions.newDefault();
		opts.setMaxClassLines(5);
		AnalysisResult resWithOpts = Maracas.analyze(
			AnalysisQuery.builder()
				.oldVersion(v1)
				.newVersion(v2)
				.client(client)
				.options(opts)
				.build());

		assertThat(resWithOpts.delta().getBreakingChanges().size(), equalTo(resWithoutOpts.delta().getBreakingChanges().size()));
		assertThat(resWithOpts.brokenClients().size(), equalTo(resWithoutOpts.brokenClients().size()));
		assertThat(resWithOpts.allBrokenUses().size(), lessThan(resWithoutOpts.allBrokenUses().size()));
	}

	@Test
	void computeDelta_isValid() {
		Delta d1 = Maracas.computeDelta(v1, v2);

		assertThat(d1, is(notNullValue()));
		assertThat(d1.getOldVersion(), is(equalTo(v1)));
		assertThat(d1.getNewVersion(), is(equalTo(v2)));
		assertThat(d1.getBreakingChanges(), everyItem(allOf(
			hasProperty("reference", is(notNullValue())),
			// TODO: uncomment once all visitors are implemented
			//hasProperty("visitor", is(notNullValue()))
			hasProperty("sourceElement", is(nullValue()))
		)));

		Delta d2 = Maracas.computeDelta(v1WithSources, v2);

		assertThat(d2, is(notNullValue()));
		assertThat(d2.getOldVersion(), is(equalTo(v1WithSources)));
		assertThat(d2.getNewVersion(), is(equalTo(v2)));
		assertThat(d2.getBreakingChanges(), everyItem(
			hasProperty("sourceElement", allOf(
				is(notNullValue()),
				hasProperty("position", is(not(instanceOf(NoSourcePosition.class))))
			)
		)));
	}

	@Test
	void computeBrokenUses_isValid() {
		Delta delta = Maracas.computeDelta(v1, v2);
		DeltaImpact deltaImpact = Maracas.computeDeltaImpact(client, delta);
		Set<BrokenUse> ds = deltaImpact.brokenUses();

		assertThat(ds, is(not(empty())));
		// No hasProperty() on records :(
		ds.forEach(d -> {
			assertThat(d.element(), allOf(
				is(notNullValue()),
				hasProperty("position", is(not(instanceOf(NoSourcePosition.class))))));
			assertThat(d.usedApiElement(), is(notNullValue()));
			assertThat(d.source(), is(notNullValue()));
		});
	}

	@Test
	void analyze_aNullQuery_throwsException() {
		assertThrows(NullPointerException.class, () ->
			Maracas.analyze(null)
		);
	}

	@Test
	void computeDelta_nullVersions_throwsException() {
		assertThrows(NullPointerException.class, () ->
			Maracas.computeDelta(v1, null)
		);

		assertThrows(NullPointerException.class, () ->
			Maracas.computeDelta(null, v2)
		);
	}

	@Test
	void computeDeltaImpact_nullClient_throwsException() {
		Delta d = Maracas.computeDelta(v1, v2);

		assertThrows(NullPointerException.class, () ->
			Maracas.computeDeltaImpact(null, d)
		);
	}

	@Test
	void computeDeltaImpact_nullDelta_throwsException() {
		assertThrows(NullPointerException.class, () ->
			Maracas.computeDeltaImpact(client, null)
		);
	}

}
