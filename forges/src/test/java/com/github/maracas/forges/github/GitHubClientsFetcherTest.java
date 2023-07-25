package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

class GitHubClientsFetcherTest {
	GitHubClientsFetcher fetcher = new GitHubClientsFetcher();
	final Repository drill = new Repository("apache", "drill", "", ""); // several modules
	final Repository ews = new Repository("OfficeDev", "ews-java-api", "", ""); // no module
	final Repository guava = new Repository("google", "guava", "", ""); // way too big

	@Test
	void fetch_modules_drill() {
		assertThat(fetcher.fetchModules(drill), hasSize(11));
	}

	@Test
	void fetch_clients_drill() {
		assertThat(fetcher.fetchClients(drill), hasSize(greaterThan(100)));
	}

	@Test
	void fetch_modules_unknown() {
		Repository unknown = new Repository("alien-tools", "unknown", "", "");
		assertThat(fetcher.fetchModules(unknown), is(empty()));
		assertThat(fetcher.fetchClients(unknown), is(empty()));
	}

	@Test
	void fetch_one_module_drill() {
		assertThat(fetcher.fetchClients(new RepositoryModule(drill, "org.apache.drill:drill-common", "")), hasSize(greaterThan(50)));
	}

	@Test
	void fetch_unknown_module_drill() {
		assertThat(fetcher.fetchClients(new RepositoryModule(drill, "unknown:module", "")), is(empty()));
	}

	@Test
	void fetch_modules_ews() {
		assertThat(fetcher.fetchModules(ews), hasSize(1));
	}

	@Test
	void fetch_clients_ews() {
		assertThat(fetcher.fetchClients(ews), hasSize(greaterThan(100)));
	}

	@Test
	void fetch_clients_guava_limit() {
		assertThat(fetcher.fetchClients(new RepositoryModule(guava, "com.google.guava:guava", ""), 1000), hasSize(1000));
	}
}
