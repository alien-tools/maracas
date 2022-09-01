package com.github.maracas.delta;

import com.github.maracas.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.hamcrest.MatcherAssert.assertThat;

class DeltaTest {
  final Library v1 = new Library(TestData.compChangesV1);
  final Library v1WithSources = new Library(TestData.compChangesV1, TestData.compChangesSources);
  final Library v2 = new Library(TestData.compChangesV2);

  @Test
  void test_JsonSerialization_WithSources() throws IOException {
    AnalysisResult res = Maracas.analyze(
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
    AnalysisResult res = Maracas.analyze(
      AnalysisQuery.builder()
        .oldVersion(v1)
        .newVersion(v2)
        .build());
    Delta delta = res.delta();

    String json = delta.toJson();
    assertThat(json, is(not(emptyOrNullString())));
  }
}