package com.github.maracas.brokenuse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

import java.io.IOException;
import java.nio.file.Path;

import com.github.maracas.*;
import org.junit.jupiter.api.Test;

class DeltaImpactTest {
  final Library v1 = new Library(TestData.compChangesV1, TestData.compChangesSources);
  final Library v2 = new Library(TestData.compChangesV2);
  final Client client = new Client(TestData.compChangesClient, v1);

  @Test
  void testJsonSerialization() throws IOException {
    AnalysisResult res = Maracas.analyze(
      AnalysisQuery.builder()
        .oldVersion(v1)
        .newVersion(v2)
        .client(client)
        .build());
    DeltaImpact deltaImpact = res.deltaImpactForClient(client);

    String json = deltaImpact.toJson();
    assertThat(json, is(not(emptyOrNullString())));
  }
}