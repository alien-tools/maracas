package com.github.maracas;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class AnalysisQueryTest {
	Path validJar = Paths.get("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");
	Path invalidJar = Paths.get("void.jar");
	Path validDirectory = Paths.get("src/main/java");
	Path validDirectory2 = Paths.get("src/test/java");
	Path invalidDirectory = Paths.get("void/");

	@Test
	void builder_correctlyCreatesQuery() {
		AnalysisQuery query = AnalysisQuery.builder()
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
		AnalysisQuery query = AnalysisQuery.builder()
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
		AnalysisQuery query = AnalysisQuery.builder()
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
			AnalysisQuery.builder().build();
		});
	}

	@Test
	void noOldJar_ThrowsException() {
		assertThrows(IllegalStateException.class, () -> {
			AnalysisQuery.builder().newJar(validJar).build();
		});
	}

	@Test
	void noNewJar_ThrowsException() {
		assertThrows(IllegalStateException.class, () -> {
			AnalysisQuery.builder().oldJar(validJar).build();
		});
	}

	@Test
	void nullOldJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().oldJar(null);
		});
	}

	@Test
	void nullNewJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().newJar(null);
		});
	}

	@Test
	void invalidOldJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().oldJar(invalidJar);
		});
	}

	@Test
	void invalidNewJar_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().newJar(invalidJar);
		});
	}

	@Test
	void nullSources_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().sources(null);
		});
	}

	@Test
	void invalidSources_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().sources(invalidDirectory);
		});
	}

	@Test
	void nullClient_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().client(null);
		});
	}

	@Test
	void invalidClient_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().client(invalidDirectory);
		});
	}

	@Test
	void nullClients_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().clients(null);
		});
	}

	@Test
	void invalidClients_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			AnalysisQuery.builder().clients(Collections.singleton(invalidDirectory));
		});
	}
}
