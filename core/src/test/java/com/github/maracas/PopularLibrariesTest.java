package com.github.maracas;

import com.github.maracas.delta.Delta;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

class PopularLibrariesTest {
  static final Path TMP_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "maracas-test-libs");
  static final String GUAVA_18 = "https://repo1.maven.org/maven2/com/google/guava/guava/18.0/guava-18.0.jar";
  static final String GUAVA_19 = "https://repo1.maven.org/maven2/com/google/guava/guava/19.0/guava-19.0.jar";
  static final String SLF4J_161 = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar";
  static final String SLF4J_172 = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.2/slf4j-api-1.7.2.jar";

  @Test
  void guava_18_to_19() {
    Path g18 = downloadJAR(GUAVA_18);
    Path g19 = downloadJAR(GUAVA_19);

    Delta d = Maracas.computeDelta(g18, g19);
    assertIsValid(d);
  }

  @Test
  void slf4j_161_to_172() {
    Path slf4j161 = downloadJAR(SLF4J_161);
    Path slf4j172 = downloadJAR(SLF4J_172);

    Delta d = Maracas.computeDelta(slf4j161, slf4j172);
    assertIsValid(d);
  }

  @BeforeAll
  static void setUp() {
    TMP_PATH.toFile().mkdirs();
  }

  @AfterAll
  static void cleanUp() throws IOException {
    FileUtils.deleteDirectory(TMP_PATH.toFile());
  }

  void assertIsValid(Delta d) {
    assertThat(d, is(notNullValue()));
    assertThat(d, is(notNullValue()));
    assertThat(d.getBreakingChanges(), everyItem(allOf(
      hasProperty("reference", is(notNullValue())),
      // TODO: uncomment once all visitors are implemented
      //hasProperty("visitor", is(notNullValue()))
      hasProperty("sourceElement", is(nullValue()))
    )));
  }

  static Path downloadJAR(String uri) {
    try {
      URL url = new URL(uri);
      String filename = url.getFile().substring(url.getFile().lastIndexOf("/") + 1, url.getFile().length());
      Path dest = TMP_PATH.resolve(filename);
      ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
      FileOutputStream fileOutputStream = new FileOutputStream(dest.toFile());
      fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
      return dest;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
