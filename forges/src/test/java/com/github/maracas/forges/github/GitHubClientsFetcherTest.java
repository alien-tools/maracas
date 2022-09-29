package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

class GitHubClientsFetcherTest {
	@Test
	void fetch_packages_spoon() {
		Repository spoon = new Repository("INRIA", "spoon", "", "");
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(spoon);
		assertThat(fetcher.fetchPackages(), hasSize(4));
	}

	@Test
	void fetch_clients_spoon() {
		Repository spoon = new Repository("INRIA", "spoon", "", "");
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(spoon);
		assertThat(fetcher.fetchClients(), is(not(empty())));
	}

	@Test
	void fetch_packages_unknown() {
		Repository unknown = new Repository("alien-tools", "unknown", "", "");
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(unknown);
		assertThat(fetcher.fetchPackages(), is(empty()));
		assertThat(fetcher.fetchClients(), is(empty()));
	}
}
