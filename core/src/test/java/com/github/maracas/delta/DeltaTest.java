package com.github.maracas.delta;

import com.github.maracas.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.hamcrest.MatcherAssert.assertThat;

class DeltaTest {
  final LibraryJar v1 = LibraryJar.withoutSources(TestData.compChangesV1);
  final LibraryJar v1WithSources = LibraryJar.withSources(TestData.compChangesV1, SourcesDirectory.of(TestData.compChangesSources));
  final LibraryJar v2 = LibraryJar.withoutSources(TestData.compChangesV2);

  @Test
  void test_JsonSerialization_WithSources() throws IOException {
    AnalysisResult res = new Maracas().analyze(
      AnalysisQuery.builder()
        .oldVersion(v1WithSources)
        .newVersion(v2)
        .build());
    Delta delta = res.delta();

    String json = delta.toJson();
    assertThat(json, is(not(emptyOrNullString())));
  }

  @Test
  void test_JsonSerialization_WithoutSources() throws IOException {
    AnalysisResult res = new Maracas().analyze(
      AnalysisQuery.builder()
        .oldVersion(v1)
        .newVersion(v2)
        .build());
    Delta delta = res.delta();

    String json = delta.toJson();
    assertThat(json, is(not(emptyOrNullString())));
  }
}