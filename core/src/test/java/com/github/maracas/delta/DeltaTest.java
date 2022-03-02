package com.github.maracas.delta;

import com.github.maracas.AnalysisQuery;
import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.TestData;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.hamcrest.MatcherAssert.assertThat;

class DeltaTest {
  Path v1 = TestData.compChangesV1;
  Path v2 = TestData.compChangesV2;
  Path sources = TestData.compChangesSources;

  @Test
  void testJsonSerialization() throws IOException {
    AnalysisResult res = Maracas.analyze(
      AnalysisQuery.builder()
        .oldJar(v1)
        .newJar(v2)
        .sources(sources)
        .build());
    Delta delta = res.delta();

    String json = delta.toJson();
    assertThat(json, is(not(emptyOrNullString())));
  }

  @Test
  void testJsonSerializationWithoutSources() throws IOException {
    AnalysisResult res = Maracas.analyze(
      AnalysisQuery.builder()
        .oldJar(v1)
        .newJar(v2)
        .build());
    Delta delta = res.delta();

    String json = delta.toJson();
    assertThat(json, is(not(emptyOrNullString())));
  }
}