package org.swat.maracas.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import fi.iki.elonen.NanoHTTPD.Response;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GithubControllerTests {
	@Autowired
	private MockMvc mvc;

	@Test
	void testSubmitAndCheckPRSync() throws Exception {
		mvc.perform(get("/github/pr-sync/tdegueul/comp-changes/2"))
    		.andExpect(status().isOk())
    		.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    		.andExpect(jsonPath("$.message", is("ok")))
    		.andExpect(jsonPath("$.delta.breakingChanges", not(empty())));
	}

	@Test
	void testSubmitPRASync() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/comp-changes/2"))
    		.andExpect(status().isAccepted())
    		.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
    		.andExpect(result -> "/github/pr/tdegueul/comp-changes/2".equals(result.getResponse().getHeader("Location")));
	}

	@Test
	void testUnknownRepository() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/NOPE/2"))
			.andExpect(status().isBadRequest());

		mvc.perform(get("/github/pr-sync/tdegueul/NOPE/2"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testUnknownPR() throws Exception {
		mvc.perform(post("/github/pr/tdegueul/comp-changes/9999"))
			.andExpect(status().isBadRequest());

		mvc.perform(get("/github/pr-sync/tdegueul/comp-changes/9999"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testPRExistsButNotAnalyzed() throws Exception {
		mvc.perform(get("/github/pr/tdegueul/comp-changes/1"))
			.andExpect(status().isNotFound());
	}
}
