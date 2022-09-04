package com.github.maracas.brokenuse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

import java.io.IOException;

import com.github.maracas.*;
import org.junit.jupiter.api.Test;

class DeltaImpactTest {
  final LibraryJar v1 = new LibraryJar(TestData.compChangesV1, new SourcesDirectory(TestData.compChangesSources));
  final LibraryJar v2 = new LibraryJar(TestData.compChangesV2);
  final SourcesDirectory client = new SourcesDirectory(TestData.compChangesClient);

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