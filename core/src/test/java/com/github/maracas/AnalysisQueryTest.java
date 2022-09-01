package com.github.maracas;

import static com.github.maracas.TestData.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import japicmp.filter.AnnotationClassFilter;
import japicmp.filter.JavadocLikeBehaviorFilter;
import japicmp.filter.JavadocLikePackageFilter;

class AnalysisQueryTest {
	AnalysisQuery.Builder builder;

	@BeforeEach
	void setUp() {
		builder = AnalysisQuery.builder();
	}

	@Test
	void builder_correctlyCreatesQuery() {
		MaracasOptions opts = MaracasOptions.newDefault();

		AnalysisQuery query = builder
			.oldVersion(validVersion)
			.newVersion(validVersion)
			.client(validClient)
			.options(opts)
			.build();

		assertThat(query.getOldVersion(), is(equalTo(validVersion)));
		assertThat(query.getNewVersion(), is(equalTo(validVersion)));
		assertThat(query.getClients(), hasSize(1));
		assertThat(query.getClients(), hasItem(equalTo(validClient)));
		assertThat(query.getMaracasOptions(), is(equalTo(opts)));
	}

	@Test
	void builder_multipleClients() {
		AnalysisQuery query = builder
			.oldVersion(validVersion)
			.newVersion(validVersion)
			.client(validClient)
			.client(validClient2)
			.client(validClient)
			.build();

		assertThat(query.getClients(), hasSize(2));
		assertThat(query.getClients(), hasItem(equalTo(validClient)));
		assertThat(query.getClients(), hasItem(equalTo(validClient2)));
	}

	@Test
	void builder_collectionOfClients() {
		AnalysisQuery query = builder
			.oldVersion(validVersion)
			.newVersion(validVersion)
			.clients(Lists.newArrayList(validClient, validClient2, validClient))
			.build();

		assertThat(query.getClients(), hasSize(2));
		assertThat(query.getClients(), hasItem(equalTo(validClient)));
		assertThat(query.getClients(), hasItem(equalTo(validClient2)));
	}

	@Test
	void builder_exclude_createsFilters() {
		AnalysisQuery query = builder
			.oldVersion(validVersion)
			.newVersion(validVersion)
			.exclude("@com.google.common.annotations.Beta")
			.exclude("*unstable*")
			.exclude("#foo()")
			.build();

			assertThat(query.getMaracasOptions().getJApiOptions().getExcludes(), hasItem(instanceOf(AnnotationClassFilter.class)));
			assertThat(query.getMaracasOptions().getJApiOptions().getExcludes(), hasItem(instanceOf(JavadocLikeBehaviorFilter.class)));
			assertThat(query.getMaracasOptions().getJApiOptions().getExcludes(), hasItem(instanceOf(JavadocLikePackageFilter.class)));
	}

	@Test
	void emptyQuery_ThrowsException() {
		assertThrows(IllegalStateException.class, () ->
			builder.build()
		);
	}

	@Test
	void noOldJar_ThrowsException() {
		builder.newVersion(validVersion);
		assertThrows(IllegalStateException.class, () ->
			builder.build()
		);
	}

	@Test
	void noNewJar_ThrowsException() {
		builder.oldVersion(validVersion);
		assertThrows(IllegalStateException.class, () ->
			builder.build()
		);
	}

	@Test
	void nullOldJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () ->
			builder.oldVersion(null)
		);
	}

	@Test
	void nullNewJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () ->
			builder.newVersion(null)
		);
	}

	@Test
	void nullClient_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () ->
			builder.client(null)
		);
	}

	@Test
	void nullClients_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () ->
			builder.clients(null)
		);
	}

	@Test
	void nullOptions_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () ->
			builder.options(null)
		);
	}

	@Test
	void nullExclude_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () ->
			builder.exclude(null)
		);
	}
}
