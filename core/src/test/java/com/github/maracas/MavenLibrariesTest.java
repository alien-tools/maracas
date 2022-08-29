package com.github.maracas;

import japicmp.model.AccessModifier;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Run the Maracas analysis on some popular libraries from Maven Central
 * and run some basic checks on the resulting models.
 */
@Tag("slow")
class MavenLibrariesTest {
	static final Path TMP_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "maracas-test-libs");

	static Stream<Arguments> popularLibraries() {
		return Stream.of(
			Arguments.of("com.google.guava", "guava", "18.0", "19.0"),
			//Arguments.of("com.google.guava",           "guava",             "30.0-jre", "31.0.1-jre"),
			Arguments.of("org.slf4j", "slf4j-api", "1.6.1", "1.7.2"),
			Arguments.of("fr.inria.gforge.spoon", "spoon-core", "7.0.0", "8.0.0"),
			Arguments.of("fr.inria.gforge.spoon", "spoon-core", "8.0.0", "9.0.0"),
			Arguments.of("fr.inria.gforge.spoon", "spoon-core", "9.0.0", "10.0.0"),
			Arguments.of("junit", "junit", "4.12", "4.13.2"),
			Arguments.of("org.junit.jupiter", "junit-jupiter-api", "5.5.2", "5.6.3"),
			Arguments.of("org.junit.jupiter", "junit-jupiter-api", "5.6.3", "5.8.2"),
			Arguments.of("commons-io", "commons-io", "2.4", "2.5"),
			Arguments.of("commons-io", "commons-io", "2.10.0", "2.11.0"),
			Arguments.of("org.apache.commons", "commons-lang3", "3.10", "3.11"),
			Arguments.of("com.google.code.gson", "gson", "2.7", "2.8.9"),
			Arguments.of("log4j", "log4j", "1.2.16", "1.2.17"),
			Arguments.of("org.assertj", "assertj-core", "3.21.0", "3.22.0"),
			Arguments.of("org.apache.httpcomponents", "httpclient", "4.4.1", "4.5"),
			Arguments.of("joda-time", "joda-time", "2.9", "2.10"),
			Arguments.of("org.mockito", "mockito-core", "3.12.4", "4.0.0"),
			Arguments.of("com.github.gumtreediff", "core", "2.1.2", "3.0.0")
		);
	}

	// The old version of the library is used as client for broken use analysis.
	@ParameterizedTest
	@MethodSource("popularLibraries")
	void test_Maven_Library_Analyses(String gid, String aid, String v1, String v2) throws IOException {
		Path oldJar = download(coordinatesToJarURL(gid, aid, v1));
		Path newJar = download(coordinatesToJarURL(gid, aid, v2));
		Path sources = downloadAndExtractSources(coordinatesToSourcesURL(gid, aid, v1));

		// Since we're using the libraries as clients themselves (so all package names clash),
		// the overhead of PACKAGE_PROTECTED is huge; stick with PROTECTED for those tests
		MaracasOptions opts = MaracasOptions.newDefault();
		opts.getJApiOptions().setAccessModifier(AccessModifier.PROTECTED);

		AnalysisQuery query = AnalysisQuery.builder()
			.oldJar(oldJar)
			.newJar(newJar)
			.client(sources)
			.options(opts)
			.build();

		AnalysisResult result = Maracas.analyze(query);
		assertThat(result.delta(), is(notNullValue()));
		System.out.println("Found %s breaking changes and %d broken uses in %s:%s (%s -> %s)".formatted(
			result.delta().getBreakingChanges().size(), result.allBrokenUses().size(), gid, aid, v1, v2));

		result.delta().getBreakingChanges().forEach(bc -> assertThat(bc.getReference(), is(notNullValue())));
		result.allBrokenUses().forEach(d -> {
			assertThat(d.element(), is(notNullValue()));
			assertThat(d.element().getPosition().isValidPosition(), is(true));
			assertThat(d.usedApiElement(), is(notNullValue()));
			assertThat(d.source(), is(notNullValue()));
		});

		result.delta().populateLocations(sources);
		result.delta().getBreakingChanges().forEach(bc -> {
			assertThat(bc.getSourceElement(), is(notNullValue()));
			assertThat(bc.getSourceElement().getPosition().isValidPosition(), is(true));
		});
	}

	static Stream<Arguments> problematicCases() {
		return Stream.of(
			Arguments.of("cn.bestwu", "starter-logging", "2.0.5", "2.0.6", "cn.bestwu.autodoc", "gen", "0.0.9")
		);
	}

	// Some cases that were failing
	@ParameterizedTest
	@MethodSource("problematicCases")
	void test_Problematic_Cases(String gid, String aid, String v1, String v2) throws IOException {
		Path oldJar = download(coordinatesToJarURL(gid, aid, v1));
		Path newJar = download(coordinatesToJarURL(gid, aid, v2));
		Path sources = downloadAndExtractSources(coordinatesToSourcesURL(gid, aid, v1));

		AnalysisQuery query = AnalysisQuery.builder()
			.oldJar(oldJar)
			.newJar(newJar)
			.client(sources)
			.build();

		AnalysisResult result = Maracas.analyze(query);
		assertThat(result.delta(), is(notNullValue()));
		System.out.println("Found %s breaking changes and %d broken uses in %s:%s (%s -> %s)".formatted(
			result.delta().getBreakingChanges().size(), result.allBrokenUses().size(), gid, aid, v1, v2));

		result.delta().getBreakingChanges().forEach(bc -> assertThat(bc.getReference(), is(notNullValue())));
		result.allBrokenUses().forEach(d -> {
			assertThat(d.element(), is(notNullValue()));
			assertThat(d.element().getPosition().isValidPosition(), is(true));
			assertThat(d.usedApiElement(), is(notNullValue()));
			assertThat(d.source(), is(notNullValue()));
		});

		result.delta().populateLocations(sources);
		result.delta().getBreakingChanges().forEach(bc -> {
			assertThat(bc.getSourceElement(), is(notNullValue()));
			assertThat(bc.getSourceElement().getPosition().isValidPosition(), is(true));
		});
	}

	@BeforeAll
	static void setUp() {
		TMP_PATH.toFile().mkdirs();
	}

	@AfterAll
	static void cleanUp() throws IOException {
		FileUtils.deleteDirectory(TMP_PATH.toFile());
	}

	String coordinatesToJarURL(String gid, String aid, String v) {
		return "https://repo1.maven.org/maven2/%s/%s/%s/%s-%s.jar".formatted(
			gid.replaceAll("\\.", "/"), aid, v, aid, v);
	}

	String coordinatesToSourcesURL(String gid, String aid, String v) {
		return "https://repo1.maven.org/maven2/%s/%s/%s/%s-%s-sources.jar".formatted(
			gid.replaceAll("\\.", "/"), aid, v, aid, v);
	}

	static Path download(String uri) {
		try {
			URL url = new URL(uri);
			String filename = url.getFile().substring(url.getFile().lastIndexOf("/") + 1);
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

	static Path downloadAndExtractSources(String uri) {
		Path sourcesJar = download(uri);
		String filename = sourcesJar.getFileName().toString();
		Path dest = TMP_PATH.resolve(filename.substring(0, filename.length() - 4));
		Path srcPath = dest.resolve("src/main/java");
		srcPath.toFile().mkdir();

		try {
			ZipFile zipFile = new ZipFile(sourcesJar.toAbsolutePath().toString());
			zipFile.extractAll(srcPath.toAbsolutePath().toString());
		} catch (ZipException e) {
			e.printStackTrace();
		}

		return dest;
	}
}
