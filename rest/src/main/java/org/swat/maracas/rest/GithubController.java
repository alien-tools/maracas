package org.swat.maracas.rest;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.PullRequestResponse;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneException;

@RestController
@RequestMapping("/github")
public class GithubController {
	@Autowired
	GithubService github;
	@Autowired
	MaracasService maracas;

	static class BreakbotRequest {
		public int installationId;
	}

	@PostMapping("/pr/{owner}/{repository}/{prId}")
	String analyzePullRequest(@PathVariable String owner, @PathVariable String repository, @PathVariable Integer prId,
		@RequestParam(required=false) String callback, @RequestHeader(required=false) String installationId, HttpServletResponse response) {
		try {
			String location = github.analyzePR(owner, repository, prId, callback, installationId);
			response.setStatus(HttpStatus.SC_ACCEPTED);
			response.setHeader("Location", location);
			return "processing";
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return e.getMessage();
		}
	}

	@GetMapping("/pr/{owner}/{repository}/{prId}")
	PullRequestResponse getPullRequest(@PathVariable String owner, @PathVariable String repository, @PathVariable Integer prId,
		HttpServletResponse response) {
		// Either we have it already
		Delta delta = github.getPullRequest(owner, repository, prId);
		if (delta != null) {
			return new PullRequestResponse("ok", delta);
		}

		// Or we're currently computing it
		if (github.isProcessing(owner, repository, prId)) {
			response.setStatus(HttpStatus.SC_ACCEPTED);
			return new PullRequestResponse("processing", null);
		}

		// Or it doesn't exist
		response.setStatus(HttpStatus.SC_NOT_FOUND);
		return new PullRequestResponse("This PR isn't being analyzed", null);
	}

	@GetMapping("/pr-sync/{owner}/{repository}/{prId}")
	PullRequestResponse analyzePullRequestDebug(@PathVariable String owner, @PathVariable String repository, @PathVariable Integer prId,
		HttpServletResponse response) {
		try {
			Delta delta = github.analyzePRSync(owner, repository, prId);
			return new PullRequestResponse("ok", delta);
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return new PullRequestResponse(e.getMessage(), null);
		} catch (CloneException | BuildException | CompletionException e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			return new PullRequestResponse(e.getMessage(), null);
		}
	}
}
