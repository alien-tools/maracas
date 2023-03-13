package com.github.maracas.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientsControllerTests {
	@Autowired
	protected MockMvc mvc;

	@Test
	void get_clients_drill() throws Exception {
		mvc.perform(get("/github/clients/apache/drill"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.owner", is("apache")))
			.andExpect(jsonPath("$.name", is("drill")))
			.andExpect(jsonPath("$.packages[*].url", hasSize(11)))
			.andExpect(jsonPath("$.clients[*].owner", is(not(empty()))));
	}

	@Test
	void get_packages_drill() throws Exception {
		mvc.perform(get("/github/packages/apache/drill"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.owner", is("apache")))
			.andExpect(jsonPath("$.name", is("drill")))
			.andExpect(jsonPath("$.packages[*].url", hasSize(11)))
			.andExpect(jsonPath("$.clients", is(empty())));
	}

	@Test
	void get_clients_unknown() throws Exception {
		mvc.perform(get("/github/clients/alien-tools/unknown"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void get_packages_unknown() throws Exception {
		mvc.perform(get("/github/packages/alien-tools/unknown"))
			.andExpect(status().isBadRequest());
	}
}
