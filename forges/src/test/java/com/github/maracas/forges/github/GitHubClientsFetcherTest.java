package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

class GitHubClientsFetcherTest {
	final Repository drill = new Repository("apache", "drill", "", ""); // several modules
	final Repository ews = new Repository("OfficeDev", "ews-java-api", "", ""); // no module
	final Repository guava = new Repository("google", "guava", "", ""); // way too big

	@Test
	void fetch_modules_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchModules(), hasSize(11));
	}

	@Test
	void fetch_clients_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchClients(), hasSize(greaterThan(100)));
	}

	@Test
	void fetch_modules_unknown() {
		Repository unknown = new Repository("alien-tools", "unknown", "", "");
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(unknown);
		assertThat(fetcher.fetchModules(), is(empty()));
		assertThat(fetcher.fetchClients(), is(empty()));
	}

	@Test
	void fetch_one_module_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchClients("org.apache.drill:drill-common"), hasSize(greaterThan(50)));
	}

	@Test
	void fetch_unknown_module_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchClients("unknown:module"), is(empty()));
	}

	@Test
	void fetch_modules_ews() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(ews);
		assertThat(fetcher.fetchModules(), hasSize(1));
	}

	@Test
	void fetch_clients_ews() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(ews);
		assertThat(fetcher.fetchClients(), hasSize(greaterThan(100)));
	}

	@Test
	void fetch_clients_guava_limit() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(guava);
		assertThat(fetcher.fetchClients("com.google.guava:guava", 1000), hasSize(1000));
	}
}
