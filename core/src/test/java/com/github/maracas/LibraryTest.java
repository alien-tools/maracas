package com.github.maracas;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

class LibraryTest {
	@Test
	void test_compChanges_withoutSources() {
		Library comp = new Library(TestData.compChangesV1);
		assertThat(comp.getJar(), is(notNullValue()));
		assertThat(comp.getLabel(), is("comp-changes-old-0.0.1.jar"));
		assertThat(comp.getSources(), is(nullValue()));
		assertThat(comp.getClasspath(), hasSize(2));
		assertThat(comp.getBinaryModel(), is(notNullValue()));
		assertThat(comp.getSourceModel(), is(nullValue()));
	}

	@Test
	void test_compChanges_withSources() {
		Library comp = new Library(TestData.compChangesV1, TestData.compChangesSources);
		assertThat(comp.getJar(), is(notNullValue()));
		assertThat(comp.getLabel(), is("comp-changes-old-0.0.1.jar"));
		assertThat(comp.getSources(), is(notNullValue()));
		assertThat(comp.getClasspath(), hasSize(2));
		assertThat(comp.getBinaryModel(), is(notNullValue()));
		assertThat(comp.getSourceModel(), is(notNullValue()));
	}
}