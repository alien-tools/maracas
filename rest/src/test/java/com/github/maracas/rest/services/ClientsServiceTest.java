package com.github.maracas.rest.services;

import com.github.maracas.forges.Repository;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ClientsServiceTest {
	ClientsService clientsService;
	Repository drill;

	@BeforeEach
	void setUp() {
		try {
			clientsService = new ClientsService(GitHubBuilder.fromEnvironment().build(), "./data-test/clients", 1);
			drill = new Repository("apache", "drill", "", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void test_drill_top_5() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(5, 0, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(drill, config, "org.apache.drill.exec:drill-rpc");
		assertThat(clients, hasSize(5));
	}

	@Test
	void test_drill_stars_100() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(5, 100, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(drill, config, "org.apache.drill.exec:drill-rpc");
		assertThat(clients, hasSize(equalTo(1)));
	}

	@Test
	void test_drill_top_and_stars() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(5, 1, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(drill, config, "org.apache.drill.exec:drill-rpc");
		assertThat(clients, hasSize(5));
	}

	@Test
	void test_drill_top_5_with_custom() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(5, 0, Collections.singletonList(
			new BreakbotConfig.GitHubRepository("a/b", "", "", "")
		));
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(drill, config, "org.apache.drill.exec:drill-rpc");
		assertThat(clients, hasSize(6));
	}

	@Test
	void test_drill_stars_10000_with_custom() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(5, 10000, Collections.singletonList(
			new BreakbotConfig.GitHubRepository("a/b", "", "", "")
		));
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(drill, config, "org.apache.drill.exec:drill-rpc");
		assertThat(clients, hasSize(equalTo(1)));
	}

	@Test
	void test_drill_only_custom() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(0, 0, Collections.singletonList(
			new BreakbotConfig.GitHubRepository("a/b", "", "", "")
		));
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(drill, config, "org.apache.drill.exec:drill-rpc");
		assertThat(clients, hasSize(equalTo(1)));
	}

	@Test
	void test_fork_has_parent_clients() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(5, 1, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(
			new Repository("break-bot", "drill-fork-for-tests", "", ""),
			config,
			"org.apache.drill.exec:drill-rpc");
		assertThat(clients, hasSize(5));
	}
}
