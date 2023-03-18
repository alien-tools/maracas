package com.github.maracas;

import org.junit.jupiter.api.Test;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.*;

class LibraryJarTest {
	@Test
	void test_compChanges_withoutSources() {
		LibraryJar comp = LibraryJar.withoutSources(TestData.compChangesV1);
		assertThat(comp.getJar(), is(notNullValue()));
		assertThat(comp.getLabel(), is("comp-changes-old-0.0.1.jar"));
		assertThat(comp.getSources(), is(nullValue()));
		assertThat(comp.getClasspath(), is(not(empty())));
		assertThat(comp.getSources(), is(nullValue()));
		assertThat(comp.buildModel(), is(notNullValue()));
	}

	@Test
	void test_compChanges_withSources() {
		LibraryJar comp = LibraryJar.withSources(TestData.compChangesV1, SourcesDirectory.of(TestData.compChangesSources));
		assertThat(comp.getJar(), is(notNullValue()));
		assertThat(comp.getLabel(), is("comp-changes-old-0.0.1.jar"));
		assertThat(comp.getSources(), is(notNullValue()));
		assertThat(comp.getClasspath(), is(not(empty())));
		assertThat(comp.buildModel(), is(notNullValue()));
		assertThat(comp.getSources(), is(notNullValue()));
		assertThat(comp.getSources().buildModel().getAllTypes(), is(not(empty())));
	}

	@Test
	void test_withoutSources_invalidJar() {
		assertThrows(IllegalArgumentException.class, () -> LibraryJar.withoutSources(TestData.invalidJar));
	}

	@Test
	void test_withSources_invalidSources() {
		LibraryJar comp = LibraryJar.withSources(TestData.compChangesV1, SourcesDirectory.of(TestData.invalidDirectory));
		assertThat(comp.getJar(), is(notNullValue()));
		assertThat(comp.getLabel(), is("comp-changes-old-0.0.1.jar"));
		assertThat(comp.getSources(), is(notNullValue()));
		assertThat(comp.getClasspath(), is(not(empty())));
		assertThat(comp.buildModel(), is(notNullValue()));
		assertThat(comp.getSources(), is(notNullValue()));
		assertThat(comp.getSources().buildModel().getAllTypes(), is(empty()));
	}

	@Test
	void test_source_binary_models_match_compChanges() {
		LibraryJar comp = LibraryJar.withSources(TestData.compChangesV1, SourcesDirectory.of(TestData.compChangesSources));
		assertSourceMatchesBinary(comp);
	}

	@Test
	void test_source_binary_models_match_jarWithDeps() {
		LibraryJar withDeps = LibraryJar.withSources(
			Path.of("./src/test/resources/jar-with-deps/target/jar-with-deps-1.0-SNAPSHOT.jar"),
			SourcesDirectory.of(Path.of("./src/test/resources/jar-with-deps")));
		assertSourceMatchesBinary(withDeps);
	}

	void assertSourceMatchesBinary(LibraryJar lib) {
		CtModel libModel = lib.buildModel();

		lib.getSources().buildModel().getAllTypes().forEach(srcType -> {
			CtType<?> binType = libModel.getRootPackage().getFactory().Type().createReference(srcType.getQualifiedName()).getTypeDeclaration();

			assertNotNull(binType, "didn't find " + srcType.getQualifiedName());
			assertEquals(srcType.getQualifiedName(), binType.getQualifiedName());

			if (srcType.isClass() || srcType.isInterface()) {
				srcType.getDeclaredExecutables().forEach(srcExec -> {
					var binExec =
						binType.getDeclaredExecutables()
							.stream()
							.filter(e -> e.getSignature().equals(srcExec.getSignature()))
							.findFirst();

					assertTrue(binExec.isPresent(), "didn't find " + srcExec.getSignature() + " in " + binType.getDeclaredExecutables());
				});

				srcType.getDeclaredFields().forEach(srcField -> {
					var binField =
						binType.getDeclaredFields()
							.stream()
							.filter(e -> e.getSimpleName().equals(srcField.getSimpleName()))
							.findFirst();

					assertTrue(binField.isPresent(), "didn't find " + srcField.getSimpleName() + " in " + binType.getDeclaredExecutables());
				});
			}
		});
	}
}