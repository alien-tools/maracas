package com.github.maracas.rest.services;

import com.github.maracas.forges.Repository;
import com.github.maracas.rest.breakbot.BreakbotConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientsServiceTest {
	@Autowired
	ClientsService clientsService;
	Repository spoon;

	@BeforeEach
	void setUp() {
		spoon = new Repository("INRIA", "spoon", "", "");
	}

	@Test
	void test_spoon_top_10() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(10, 0, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(spoon, config, "fr.inria.gforge.spoon:spoon-core");
		assertThat(clients, hasSize(10));
	}

	@Test
	void test_spoon_stars_100() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(0, 100, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(spoon, config, "fr.inria.gforge.spoon:spoon-core");
		assertThat(clients, not(empty()));
	}

	@Test
	void test_spoon_top_and_stars() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(10, 1, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(spoon, config, "fr.inria.gforge.spoon:spoon-core");
		assertThat(clients, hasSize(10));
	}

	@Test
	void test_spoon_top_10_with_custom() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(10, 0, Collections.singletonList(
			new BreakbotConfig.GitHubRepository("a/b", "", "", "")
		));
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(spoon, config, "fr.inria.gforge.spoon:spoon-core");
		assertThat(clients, hasSize(11));
	}

	@Test
	void test_spoon_stars_10_with_custom() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(0, 10, Collections.singletonList(
			new BreakbotConfig.GitHubRepository("a/b", "", "", "")
		));
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(spoon, config, "fr.inria.gforge.spoon:spoon-core");
		assertThat(clients, hasSize(greaterThan(1)));
	}

	@Test
	void test_fork_has_parent_clients() {
		BreakbotConfig.Clients config = new BreakbotConfig.Clients(10, 0, Collections.emptyList());
		List<BreakbotConfig.GitHubRepository> clients = clientsService.buildClientsList(
			new Repository("break-bot", "flowable-engine", "", ""),
			config,
			"org.flowable:flowable-engine");
		assertThat(clients, hasSize(10));
	}
}
