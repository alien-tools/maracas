package com.github.maracas.brokenuse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.github.maracas.AnalysisQuery;
import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.TestData;

class DeltaImpactTest {
  final Path v1 = TestData.compChangesV1;
  final Path v2 = TestData.compChangesV2;
  final Path sources = TestData.compChangesSources;
  final Path client = TestData.compChangesClient;

  @Test
  void testJsonSerialization() throws IOException {
    AnalysisResult res = Maracas.analyze(
      AnalysisQuery.builder()
        .oldJar(v1)
        .newJar(v2)
        .sources(sources)
        .client(client)
        .build());
    DeltaImpact deltaImpact = res.deltaImpactForClient(client);

    String json = deltaImpact.toJson();
    assertThat(json, is(not(emptyOrNullString())));
  }
}