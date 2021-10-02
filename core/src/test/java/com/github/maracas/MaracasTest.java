package com.github.maracas;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.github.maracas.delta.Delta;
import com.github.maracas.detection.Detection;

import japicmp.config.Options;
import japicmp.model.AccessModifier;

class MaracasTest {
	Path v1 = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	Path v2 = Paths.get("../test-data/comp-changes/new/target/comp-changes-new-0.0.1.jar");
	Path client = Paths.get("../test-data/comp-changes/client/src/");
	Path client2 = Paths.get("../test-data/comp-changes/old/src/");
	Path sources = Paths.get("../test-data/comp-changes/old/src/");

	@Test
	void analyze_aNullQuery_throwsException() {
		assertThrows(NullPointerException.class, () ->
			Maracas.analyze(null)
		);
	}

	@Test
	void analyze_QueryWithoutClient_hasNoDetection() {
		AnalysisResult res = Maracas.analyze(
			AnalysisQuery.builder()
				.oldJar(v1)
				.newJar(v2)
				.build());

		assertThat(res.delta(), is(notNullValue()));
		assertThat(new ArrayList<>(res.delta().getBrokenDeclarations()),
			everyItem(hasProperty("sourceElement", nullValue())));
		assertThat(res.allDetections(), is(empty()));
	}

	@Test
	void analyze_QueryWithTwoClients_hasTwoClientDetections() {
		AnalysisResult res = Maracas.analyze(
			AnalysisQuery.builder()
				.oldJar(v1)
				.newJar(v2)
				.client(client)
				.client(client2)
				.build());

		assertThat(res.delta(), is(notNullValue()));
		assertThat(res.detections().keySet(), hasSize(2));
		assertThat(res.detections(), hasKey(client.toAbsolutePath()));
		assertThat(res.detections(), hasKey(client2.toAbsolutePath()));
	}

	@Test
	void analyze_QueryWithSources_hasSourceLocations() {
		AnalysisResult res = Maracas.analyze(
			AnalysisQuery.builder()
				.oldJar(v1)
				.newJar(v2)
				.sources(sources)
				.build());

		assertThat(res.delta(), is(notNullValue()));
		assertThat(new ArrayList<>(res.delta().getBrokenDeclarations()),
			everyItem(hasProperty("sourceElement", notNullValue())));
	}

	@Test
	void analyze_QueryWithOptions_UsesOptions() {
		Options publicOpts = Maracas.defaultJApiOptions();
		publicOpts.setAccessModifier(AccessModifier.PUBLIC);

		Options privateOpts = Maracas.defaultJApiOptions();
		privateOpts.setAccessModifier(AccessModifier.PRIVATE);

		AnalysisResult resPublic = Maracas.analyze(
			AnalysisQuery.builder()
				.oldJar(v1)
				.newJar(v2)
				.jApiOptions(publicOpts)
				.build());

		AnalysisResult resPrivate = Maracas.analyze(
			AnalysisQuery.builder()
				.oldJar(v1)
				.newJar(v2)
				.jApiOptions(privateOpts)
				.build());

		assertThat(resPublic.delta().getBrokenDeclarations().size(),
			is(not(equalTo(resPrivate.delta().getBrokenDeclarations().size()))));
	}

	@Test
	void computeDelta_invalidPaths_throwsException() {
		assertThrows(IllegalArgumentException.class, () ->
			Maracas.computeDelta(v1, null)
		);

		assertThrows(IllegalArgumentException.class, () ->
			Maracas.computeDelta(null, v2)
		);

		assertThrows(IllegalArgumentException.class, () ->
			Maracas.computeDelta(v1, TestData.invalidJar)
		);

		assertThrows(IllegalArgumentException.class, () ->
			Maracas.computeDelta(TestData.invalidJar, v2)
		);
	}

	@Test
	void computeDelta_isValid() {
		Delta d = Maracas.computeDelta(v1, v2);

		assertThat(d, is(notNullValue()));
		assertThat(d.getOldJar(), is(equalTo(v1.toAbsolutePath())));
		assertThat(d.getNewJar(), is(equalTo(v2.toAbsolutePath())));
	}

	@Test
	void computeDetections_invalidPaths_throwsException() {
		Delta d = Maracas.computeDelta(v1, v2);
		assertThrows(IllegalArgumentException.class, () ->
			Maracas.computeDetections(TestData.invalidDirectory, d)
		);

		assertThrows(IllegalArgumentException.class, () ->
			Maracas.computeDetections(null, d)
		);
	}

	@Test
	void computeDetections_nullDelta_throwsException() {
		assertThrows(NullPointerException.class, () ->
			Maracas.computeDetections(v1, null)
		);
	}

	@Test
	void computeDetections_isValid() {
		Delta d = Maracas.computeDelta(v1, v2);
		Collection<Detection> ds = Maracas.computeDetections(client, d);

		assertThat(ds, is(not(empty())));
	}

}
