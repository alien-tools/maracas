package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import com.github.maracas.forges.RepositoryModule;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

class GitHubClientsScraperTest {
	GitHubClientsScraper fetcher = new GitHubClientsScraper(Duration.ofDays(1));
	final Repository spoon = new Repository("INRIA", "spoon", "", ""); // several modules
	final Repository ews = new Repository("OfficeDev", "ews-java-api", "", ""); // no module
	final Repository guava = new Repository("google", "guava", "", ""); // way too big

	@Test
	void fetch_modules_spoon() {
		assertThat(fetcher.fetchModules(spoon), hasSize(5));
	}

	@Test
	void fetch_clients_spoon() {
		assertThat(fetcher.fetchClients(spoon, GitHubClientsFetcher.ClientFilter.ALL, 100), hasSize(greaterThanOrEqualTo(100)));
	}

	@Test
	void fetch_modules_unknown() {
		Repository unknown = new Repository("alien-tools", "unknown", "", "");
		assertThat(fetcher.fetchModules(unknown), is(empty()));
		assertThat(fetcher.fetchClients(unknown, GitHubClientsFetcher.ClientFilter.ALL, 1), is(empty()));
	}

	@Test
	void fetch_one_module_spoon() {
		assertThat(fetcher.fetchClients(new RepositoryModule(spoon, "fr.inria.gforge.spoon:spoon-core", ""), GitHubClientsFetcher.ClientFilter.ALL, 50), hasSize(50));
	}

	@Test
	void fetch_unknown_module_spoon() {
		assertThat(fetcher.fetchClients(new RepositoryModule(spoon, "unknown:module", ""), GitHubClientsFetcher.ClientFilter.ALL, 1), is(empty()));
	}

	@Test
	void fetch_modules_ews() {
		assertThat(fetcher.fetchModules(ews), hasSize(1));
	}

	@Test
	void fetch_clients_ews() {
		assertThat(fetcher.fetchClients(ews, GitHubClientsFetcher.ClientFilter.ALL, 100), hasSize(100));
	}

	@Test
	void fetch_clients_guava_limit() {
		assertThat(fetcher.fetchClients(new RepositoryModule(guava, "com.google.guava:guava", ""), GitHubClientsFetcher.ClientFilter.ALL, 1000), hasSize(1000));
	}
}
