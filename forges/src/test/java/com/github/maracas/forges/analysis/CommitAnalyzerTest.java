package com.github.maracas.forges.analysis;

import com.github.maracas.AnalysisResult;
import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.MaracasOptions;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.delta.Delta;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.build.CommitBuilder;
import com.github.maracas.forges.clone.CloneException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.gt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommitAnalyzerTest {
	@Mock
	Maracas maracas;
	CommitAnalyzer analyzer;
	Path jar = Path.of("../test-data/comp-changes/old/target/comp-changes-old-0.0.1.jar");

	@BeforeEach
	void setUp() {
		analyzer = new CommitAnalyzer(maracas, Executors.newFixedThreadPool(4));
	}

	@Test
	void computeDelta_success() {
		Path module = Path.of("moduleA");
		LibraryJar v1 = LibraryJar.withSources(jar, SourcesDirectory.of(module));
		LibraryJar v2 = LibraryJar.withoutSources(jar);
		CommitBuilder builderV1 = mock();
		CommitBuilder builderV2 = mock();
		Delta emptyDelta = new Delta(v1, v2, Collections.emptyList());
		MaracasOptions opts = MaracasOptions.newDefault();

		when(builderV1.getModulePath()).thenReturn(module);
		when(builderV1.buildCommit(gt(0))).thenReturn(Optional.of(jar));
		when(builderV2.buildCommit(gt(0))).thenReturn(Optional.of(jar));
		when(maracas.computeDelta(v1, v2, opts)).thenReturn(emptyDelta);

		Delta res = analyzer.computeDelta(builderV1, builderV2, opts);

		assertThat(res.getOldVersion(), is(equalTo(v1)));
		assertThat(res.getNewVersion(), is(equalTo(v2)));
		assertThat(res.getBreakingChanges(), is(empty()));

		verify(builderV1).cloneCommit(gt(0));
		verify(builderV1).buildCommit(gt(0));
		verify(builderV2).cloneCommit(gt(0));
		verify(builderV2).buildCommit(gt(0));
		verify(maracas).computeDelta(v1, v2, opts);
	}

	@Test
	void computeDelta_cloneException() {
		CommitBuilder failedBuilder = mock();
		CommitBuilder successBuilder = mock();
		MaracasOptions opts = MaracasOptions.newDefault();

		doThrow(new CloneException("nope")).when(failedBuilder).cloneCommit(gt(0));

		Exception thrown = assertThrows(CloneException.class, () -> analyzer.computeDelta(failedBuilder, successBuilder, opts));

		assertThat(thrown.getMessage(), is(equalTo("nope")));

		verify(failedBuilder).cloneCommit(gt(0));
		verify(failedBuilder, never()).buildCommit(gt(0));
		verify(successBuilder, atMostOnce()).cloneCommit(gt(0));
		verify(successBuilder, atMostOnce()).buildCommit(gt(0));
		verify(maracas, never()).computeDelta(any(), any(), any());
	}

	@Test
	void computeDelta_buildException() {
		CommitBuilder successBuilder = mock();
		CommitBuilder failedBuilder = mock();
		MaracasOptions opts = MaracasOptions.newDefault();

		when(successBuilder.buildCommit(gt(0))).thenReturn(Optional.of(jar));
		when(failedBuilder.buildCommit(gt(0))).thenThrow(new BuildException("nope"));

		Exception thrown = assertThrows(BuildException.class, () -> analyzer.computeDelta(successBuilder, failedBuilder, opts));

		assertThat(thrown.getMessage(), is(equalTo("nope")));

		verify(successBuilder, atMostOnce()).cloneCommit(gt(0));
		verify(successBuilder, atMostOnce()).buildCommit(gt(0));
		verify(failedBuilder).cloneCommit(gt(0));
		verify(failedBuilder).buildCommit(gt(0));
		verify(maracas, never()).computeDelta(any(), any(), any());
	}

	@Test
	void computeDelta_no_JAR_created() {
		CommitBuilder builderV1 = mock();
		CommitBuilder builderV2 = mock();
		MaracasOptions opts = MaracasOptions.newDefault();

		when(builderV1.buildCommit(gt(0))).thenReturn(Optional.empty());
		when(builderV2.buildCommit(gt(0))).thenReturn(Optional.of(jar));

		Exception thrown = assertThrows(BuildException.class, () -> analyzer.computeDelta(builderV1, builderV2, opts));

		assertThat(thrown.getMessage(), containsString("Couldn't find the JAR"));

		verify(builderV1).cloneCommit(gt(0));
		verify(builderV1).buildCommit(gt(0));
		verify(builderV2).cloneCommit(gt(0));
		verify(builderV2).buildCommit(gt(0));
		verify(maracas, never()).computeDelta(any(), any(), any());
	}

	@Test
	void computeImpact_two_clients_success() {
		Delta delta = new Delta(mock(), mock(), List.of(mock(BreakingChange.class)));
		MaracasOptions opts = MaracasOptions.newDefault();

		Path notBrokenModule = Path.of("not-broken/module");
		Path brokenModule = Path.of("broken/module");
		SourcesDirectory notBrokenSources = SourcesDirectory.of(notBrokenModule);
		SourcesDirectory brokenSources = SourcesDirectory.of(brokenModule);
		CommitBuilder notBrokenClient = mock();
		CommitBuilder brokenClient = mock();

		when(notBrokenClient.getModulePath()).thenReturn(notBrokenModule);
		when(brokenClient.getModulePath()).thenReturn(brokenModule);
		when(maracas.computeDeltaImpact(any(), any(), any())).thenAnswer(invocation -> {
			if (invocation.getArgument(0).equals(notBrokenSources))
				return DeltaImpact.success(notBrokenSources, delta, Collections.emptySet());
			else
				return DeltaImpact.success(brokenSources, delta, Set.of(mock(BrokenUse.class)));
		});

		AnalysisResult res = analyzer.computeImpact(delta, List.of(notBrokenClient, brokenClient), opts);

		assertThat(res.delta().getBreakingChanges(), hasSize(1));
		assertThat(res.deltaImpacts(), is(aMapWithSize(2)));
		DeltaImpact notBrokenImpact = res.deltaImpacts().get(notBrokenSources);
		assertThat(notBrokenImpact.brokenUses(), is(empty()));
		assertThat(notBrokenImpact.throwable(), is(nullValue()));
		DeltaImpact brokenImpact = res.deltaImpacts().get(brokenSources);
		assertThat(brokenImpact.brokenUses(), hasSize(1));
		assertThat(brokenImpact.throwable(), is(nullValue()));

		verify(notBrokenClient).cloneCommit(gt(0));
		verify(brokenClient).cloneCommit(gt(0));
		verify(maracas).computeDeltaImpact(notBrokenSources, delta, opts);
		verify(maracas).computeDeltaImpact(brokenSources, delta, opts);
	}

	@Test
	void computeImpact_two_clients_one_analysis_fails() {
		Delta delta = new Delta(mock(), mock(), List.of(mock(BreakingChange.class)));
		MaracasOptions opts = MaracasOptions.newDefault();

		Path failedModule = Path.of("failed-client/module");
		Path successModule = Path.of("success-client/module");
		SourcesDirectory failedSources = SourcesDirectory.of(failedModule);
		SourcesDirectory successSources = SourcesDirectory.of(successModule);
		CommitBuilder failedClient = mock();
		CommitBuilder successClient = mock();

		when(failedClient.getModulePath()).thenReturn(failedModule);
		when(successClient.getModulePath()).thenReturn(successModule);
		when(maracas.computeDeltaImpact(any(), any(), any())).thenAnswer(invocation -> {
			if (invocation.getArgument(0).equals(failedSources))
				return DeltaImpact.error(failedSources, delta, new RuntimeException("nope"));
			else
				return DeltaImpact.success(successSources, delta, Set.of(mock(BrokenUse.class)));
		});

		AnalysisResult res = analyzer.computeImpact(delta, List.of(failedClient, successClient), opts);

		assertThat(res.delta().getBreakingChanges(), hasSize(1));
		assertThat(res.deltaImpacts(), is(aMapWithSize(2)));
		DeltaImpact failedImpact = res.deltaImpacts().get(failedSources);
		assertThat(failedImpact.brokenUses(), is(empty()));
		assertThat(failedImpact.throwable(), is(not(nullValue())));
		assertThat(failedImpact.throwable().getMessage(), is(equalTo("nope")));
		DeltaImpact successImpact = res.deltaImpacts().get(successSources);
		assertThat(successImpact.brokenUses(), hasSize(1));
		assertThat(successImpact.throwable(), is(nullValue()));

		verify(failedClient).cloneCommit(gt(0));
		verify(successClient).cloneCommit(gt(0));
		verify(maracas).computeDeltaImpact(failedSources, delta, opts);
		verify(maracas).computeDeltaImpact(successSources, delta, opts);
	}

	@Test
	void computeImpact_two_clients_one_clone_timeout() {
		Delta delta = new Delta(mock(), mock(), List.of(mock(BreakingChange.class)));
		MaracasOptions opts = MaracasOptions.newDefault();

		Path timeoutModule = Path.of("client-timeout/module");
		Path successModule = Path.of("client/module");
		SourcesDirectory timeoutSources = SourcesDirectory.of(timeoutModule);
		SourcesDirectory successSources = SourcesDirectory.of(successModule);
		CommitBuilder timeoutClient = mock();
		CommitBuilder successClient = mock();

		when(timeoutClient.getModulePath()).thenReturn(timeoutModule);
		when(successClient.getModulePath()).thenReturn(successModule);
		doThrow(new CloneException("nope")).when(timeoutClient).cloneCommit(gt(0));
		when(maracas.computeDeltaImpact(any(), any(), any())).thenReturn(DeltaImpact.success(successSources, delta, Set.of(mock(BrokenUse.class))));

		AnalysisResult res = analyzer.computeImpact(delta, List.of(timeoutClient, successClient), opts);

		assertThat(res.delta().getBreakingChanges(), hasSize(1));
		assertThat(res.deltaImpacts(), is(aMapWithSize(2)));
		DeltaImpact timeoutImpact = res.deltaImpacts().get(timeoutSources);
		assertThat(timeoutImpact.brokenUses(), is(empty()));
		assertThat(timeoutImpact.throwable(), is(not(nullValue())));
		assertThat(timeoutImpact.throwable().getMessage(), is(equalTo("nope")));
		DeltaImpact successImpact = res.deltaImpacts().get(successSources);
		assertThat(successImpact.brokenUses(), hasSize(1));
		assertThat(successImpact.throwable(), is(nullValue()));

		verify(timeoutClient).cloneCommit(gt(0));
		verify(successClient).cloneCommit(gt(0));
		verify(maracas).computeDeltaImpact(successSources, delta, opts);
	}

	@Test
	void computeImpact_no_client() {
		Path module = Path.of("moduleA");
		LibraryJar v1 = LibraryJar.withSources(jar, SourcesDirectory.of(module));
		LibraryJar v2 = LibraryJar.withoutSources(jar);
		BreakingChange bc = mock();
		Delta delta = new Delta(v1, v2, List.of(bc));
		MaracasOptions opts = MaracasOptions.newDefault();

		AnalysisResult res = analyzer.computeImpact(delta, Collections.emptyList(), opts);

		assertThat(res.delta().getBreakingChanges(), hasSize(1));
		assertThat(res.deltaImpacts(), is(anEmptyMap()));

		verify(maracas, never()).computeDeltaImpact(any(), any(), any());
	}

	@Test
	void computeImpact_no_BC_in_delta() {
		Path module = Path.of("moduleA");
		LibraryJar v1 = LibraryJar.withSources(jar, SourcesDirectory.of(module));
		LibraryJar v2 = LibraryJar.withoutSources(jar);
		Delta emptyDelta = new Delta(v1, v2, Collections.emptyList());
		MaracasOptions opts = MaracasOptions.newDefault();

		CommitBuilder client1 = mock();
		CommitBuilder client2 = mock();

		when(client1.getClonePath()).thenReturn(Path.of("client1"));
		when(client2.getClonePath()).thenReturn(Path.of("client2"));

		AnalysisResult res = analyzer.computeImpact(emptyDelta, List.of(client1, client2), opts);

		assertThat(res.delta().getBreakingChanges(), is(empty()));
		assertThat(res.deltaImpacts(), is(aMapWithSize(2)));

		verify(maracas, never()).computeDeltaImpact(any(), any(), any());
	}

	@Test
	void analyzeCommits_success() {
		Path module = Path.of("moduleA");
		LibraryJar v1 = LibraryJar.withSources(jar, SourcesDirectory.of(module));
		LibraryJar v2 = LibraryJar.withoutSources(jar);
		Delta emptyDelta = new Delta(v1, v2, Collections.emptyList());
		MaracasOptions opts = MaracasOptions.newDefault();

		CommitBuilder builderV1 = mock();
		CommitBuilder builderV2 = mock();

		when(builderV1.getModulePath()).thenReturn(module);
		when(builderV1.buildCommit(gt(0))).thenReturn(Optional.of(jar));
		when(builderV2.buildCommit(gt(0))).thenReturn(Optional.of(jar));
		when(maracas.computeDelta(v1, v2, opts)).thenReturn(emptyDelta);

		AnalysisResult res = analyzer.analyzeCommits(builderV1, builderV2, Collections.emptyList(), opts);

		assertThat(res.error(), is(nullValue()));
		assertThat(res.delta().getBreakingChanges(), is(empty()));
		assertThat(res.deltaImpacts(), is(aMapWithSize(0)));

		verify(builderV1).cloneCommit(gt(0));
		verify(builderV1).buildCommit(gt(0));
		verify(builderV2).cloneCommit(gt(0));
		verify(builderV2).buildCommit(gt(0));
		verify(maracas).computeDelta(v1, v2, opts);
	}
}
