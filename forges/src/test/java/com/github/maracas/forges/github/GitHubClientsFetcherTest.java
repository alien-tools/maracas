package com.github.maracas.forges.github;

import com.github.maracas.forges.Repository;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GitHubClientsFetcherTest {
	@Test
	void fetch_packages_spoon() throws IOException {
		Repository spoon = new Repository("INRIA", "spoon", "", "");
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(spoon);
		System.out.println(fetcher.fetchPackages());
	}

	@Test
	void fetch_packages_guava() throws IOException {
		Repository spoon = new Repository("google", "guava", "", "");
		GitHubClientsFetcher fetcher = new GitHubClientsFetcher(spoon);
		System.out.println(fetcher.fetchPackages());
	}
}