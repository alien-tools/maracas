package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

class GitHubClientsFetcherTest {
	final Repository drill = new Repository("apache", "drill", "", "");

	@Test
	void fetch_packages_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchPackages(), hasSize(11));
	}

	@Test
	void fetch_clients_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchClients(), is(not(empty())));
	}

	@Test
	void fetch_packages_unknown() {
		Repository unknown = new Repository("alien-tools", "unknown", "", "");
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(unknown);
		assertThat(fetcher.fetchPackages(), is(empty()));
		assertThat(fetcher.fetchClients(), is(empty()));
	}

	@Test
	void fetch_one_package_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchClients("org.apache.drill.exec:drill-rpc"), is(not(empty())));
	}

	@Test
	void fetch_unknown_package_drill() {
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(drill);
		assertThat(fetcher.fetchClients("unknown:package"), is(empty()));
	}
}
