package com.github.maracas;

import static com.github.maracas.TestData.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class AnalysisQueryTest {
	AnalysisQuery.Builder builder;

	@BeforeEach
	void setUp() {
		builder = AnalysisQuery.builder();
	}

	@Test
	void builder_correctlyCreatesQuery() {
		AnalysisQuery query = builder
			.oldJar(validJar)
			.newJar(validJar)
			.client(validDirectory)
			.sources(validDirectory)
			.build();

		assertThat(query.getOldJar(), is(equalTo(validJar.toAbsolutePath())));
		assertThat(query.getNewJar(), is(equalTo(validJar.toAbsolutePath())));
		assertThat(query.getClients(), hasSize(1));
		assertThat(query.getClients(), hasItem(equalTo(validDirectory.toAbsolutePath())));
		assertThat(query.getSources(), is(equalTo(validDirectory.toAbsolutePath())));
	}

	@Test
	void builder_multipleClients() {
		AnalysisQuery query = builder
			.oldJar(validJar)
			.newJar(validJar)
			.client(validDirectory)
			.client(validDirectory2)
			.client(validDirectory)
			.build();

		assertThat(query.getClients(), hasSize(2));
		assertThat(query.getClients(), hasItem(equalTo(validDirectory.toAbsolutePath())));
		assertThat(query.getClients(), hasItem(equalTo(validDirectory2.toAbsolutePath())));
	}

	@Test
	void builder_collectionOfClients() {
		AnalysisQuery query = builder
			.oldJar(validJar)
			.newJar(validJar)
			.clients(Lists.newArrayList(validDirectory, validDirectory2, validDirectory))
			.build();

		assertThat(query.getClients(), hasSize(2));
		assertThat(query.getClients(), hasItem(equalTo(validDirectory.toAbsolutePath())));
		assertThat(query.getClients(), hasItem(equalTo(validDirectory2.toAbsolutePath())));
	}

	@Test
	void emptyQuery_ThrowsException() {
		assertThrows(IllegalStateException.class, () -> {
			builder.build();
		});
	}

	@Test
	void noOldJar_ThrowsException() {
		builder.newJar(validJar);
		assertThrows(IllegalStateException.class, () -> {
			builder.build();
		});
	}

	@Test
	void noNewJar_ThrowsException() {
		builder.oldJar(validJar);
		assertThrows(IllegalStateException.class, () -> {
			builder.build();
		});
	}

	@Test
	void nullOldJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.oldJar(null);
		});
	}

	@Test
	void nullNewJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.newJar(null);
		});
	}

	@Test
	void invalidOldJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.oldJar(invalidJar);
		});
	}

	@Test
	void invalidNewJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.newJar(invalidJar);
		});
	}

	@Test
	void nullSources_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.sources(null);
		});
	}

	@Test
	void invalidSources_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.sources(invalidDirectory);
		});
	}

	@Test
	void nullClient_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.client(null);
		});
	}

	@Test
	void invalidClient_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.client(invalidDirectory);
		});
	}

	@Test
	void nullClients_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.clients(null);
		});
	}

	@Test
	void invalidClients_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			builder.clients(Collections.singleton(invalidDirectory));
		});
	}
}
