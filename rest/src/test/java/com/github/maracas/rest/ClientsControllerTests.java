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
	void get_clients_spoon() throws Exception {
		mvc.perform(get("/github/clients/INRIA/spoon"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.owner", is("INRIA")))
			.andExpect(jsonPath("$.name", is("spoon")))
			.andExpect(jsonPath("$.modules[*].url", hasSize(5)))
			.andExpect(jsonPath("$.clients[*].owner", is(not(empty()))));
	}

	@Test
	void get_modules_spoon() throws Exception {
		mvc.perform(get("/github/modules/INRIA/spoon"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.owner", is("INRIA")))
			.andExpect(jsonPath("$.name", is("spoon")))
			.andExpect(jsonPath("$.modules[*].url", hasSize(5)))
			.andExpect(jsonPath("$.clients", is(empty())));
	}

	@Test
	void get_clients_unknown() throws Exception {
		mvc.perform(get("/github/clients/alien-tools/unknown"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void get_modules_unknown() throws Exception {
		mvc.perform(get("/github/modules/alien-tools/unknown"))
			.andExpect(status().isBadRequest());
	}
}
