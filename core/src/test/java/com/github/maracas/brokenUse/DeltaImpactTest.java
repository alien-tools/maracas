package com.github.maracas.brokenUse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maracas.AnalysisQuery;
import com.github.maracas.AnalysisResult;
import com.github.maracas.Maracas;
import com.github.maracas.TestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

class BrokenUseTest {
  Path v1 = TestData.compChangesV1;
  Path v2 = TestData.compChangesV2;
  Path sources = TestData.compChangesSources;
  Path client = TestData.compChangesClient;

  @Test
  void testJsonSerialization() throws IOException {
    AnalysisResult res = Maracas.analyze(
      AnalysisQuery.builder()
        .oldJar(v1)
        .newJar(v2)
        .sources(sources)
        .client(client)
        .build());
    Collection<BrokenUse> brokenUses = res.allBrokenUses();

    String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(brokenUses);
    assertThat(json, is(not(emptyOrNullString())));
  }
}