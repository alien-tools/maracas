package com.github.maracas;

import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtType;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LibraryJarTest {
	@Test
	void test_compChanges_withoutSources() {
		LibraryJar comp = new LibraryJar(TestData.compChangesV1);
		assertThat(comp.getJar(), is(notNullValue()));
		assertThat(comp.getLabel(), is("comp-changes-old-0.0.1.jar"));
		assertThat(comp.getSources(), is(nullValue()));
		assertThat(comp.getClasspath(), hasSize(2));
		assertThat(comp.getModel(), is(notNullValue()));
	}

	@Test
	void test_compChanges_withSources() {
		LibraryJar comp = new LibraryJar(TestData.compChangesV1, new SourcesDirectory(TestData.compChangesSources));
		assertThat(comp.getJar(), is(notNullValue()));
		assertThat(comp.getLabel(), is("comp-changes-old-0.0.1.jar"));
		assertThat(comp.getSources(), is(notNullValue()));
		assertThat(comp.getClasspath(), hasSize(2));
		assertThat(comp.getModel(), is(notNullValue()));
		assertThat(comp.getSources().getModel(), is(notNullValue()));
	}

	@Test
	void test_source_binary_models_match_compChanges() {
		LibraryJar comp = new LibraryJar(TestData.compChangesV1, new SourcesDirectory(TestData.compChangesSources));
		assertSourceMatchesBinary(comp);
	}

	@Test
	void test_source_binary_models_match_jarWithDeps() {
		LibraryJar withDeps = new LibraryJar(Paths.get("./src/test/resources/jar-with-deps/target/jar-with-deps-1.0-SNAPSHOT.jar"),
			new SourcesDirectory(Paths.get("./src/test/resources/jar-with-deps")));
		assertSourceMatchesBinary(withDeps);
	}

	void assertSourceMatchesBinary(LibraryJar lib) {
		lib.getSources().getModel().getAllTypes().forEach(srcType -> {
			CtType<?> binType = lib.getModel().getRootPackage().getFactory().Type().createReference(srcType.getQualifiedName()).getTypeDeclaration();

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