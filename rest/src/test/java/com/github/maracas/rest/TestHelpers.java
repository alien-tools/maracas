package com.github.maracas.rest;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public class TestHelpers {
	public static ResultActions checkReportIsValid(ResultActions res) throws Exception {
		return res
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message", is("ok")));
	}

	public static ResultActions checkReportHasDelta(ResultActions res) throws Exception {
		return checkReportIsValid(res)
			.andExpect(jsonPath("$.report.delta.brokenDeclarations", not(empty())));
	}

	public static ResultActions checkReportHasDetections(ResultActions res) throws Exception {
		return checkReportHasDelta(res)
			.andExpect(jsonPath("$.report.clientDetections", not(empty())));
	}

	public static ResultActions checkReportHasNoDetection(ResultActions res) throws Exception {
		return checkReportHasDelta(res)
			.andExpect(jsonPath("$.report.clientDetections", is(empty())));
	}

	// Sigh
	public static ResultActions waitForPRAnalysis(MockMvc mvc, String uri) throws Exception {
		CompletableFuture<ResultActions> future = CompletableFuture.supplyAsync(() -> {
			ResultActions res = null;
			try {
				do {
					res = mvc.perform(get(uri));
					Thread.sleep(1000);
				} while(res.andReturn().getResponse().getStatus() != HttpStatus.SC_OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return res;
		});

		return future.get(30, TimeUnit.SECONDS);
	}
}
