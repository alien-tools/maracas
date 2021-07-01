package org.swat.maracas.rest;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swat.maracas.rest.data.MaracasReport;
import org.swat.maracas.rest.data.PullRequestResponse;
import org.swat.maracas.rest.tasks.BuildException;
import org.swat.maracas.rest.tasks.CloneException;

@RestController
@RequestMapping("/github")
public class GithubController {
	@Autowired
	GithubService github;

	private static final Logger logger = LogManager.getLogger(GithubController.class);

	@PostMapping("/pr/{owner}/{repository}/{prId}")
	public PullRequestResponse analyzePullRequest(@PathVariable String owner, @PathVariable String repository, @PathVariable Integer prId,
		@RequestParam(required=false) String callback, @RequestHeader(required=false) String installationId, HttpServletResponse response) {
		try {
			String location = github.analyzePR(owner, repository, prId, callback, installationId);
			response.setStatus(HttpStatus.SC_ACCEPTED);
			response.setHeader("Location", location);
			return new PullRequestResponse("processing", null);
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			logger.error(e);
			return new PullRequestResponse(e.getMessage(), null);
		} catch (Exception e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.error(e);
			return new PullRequestResponse(e.getMessage(), null);
		}
	}

	@GetMapping("/pr/{owner}/{repository}/{prId}")
	public PullRequestResponse getPullRequest(@PathVariable String owner, @PathVariable String repository, @PathVariable Integer prId,
		HttpServletResponse response) {
		try {
			// Either we have it already
			MaracasReport report = github.getReport(owner, repository, prId);
			if (report != null) {
				return new PullRequestResponse("ok", report);
			}

			// Or we're currently computing it
			if (github.isProcessing(owner, repository, prId)) {
				response.setStatus(HttpStatus.SC_PROCESSING);
				return new PullRequestResponse("processing", null);
			}

			// Or it doesn't exist
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return new PullRequestResponse("This PR isn't being analyzed", null);
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			logger.error(e);
			return new PullRequestResponse(e.getMessage(), null);
		} catch (Exception e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.error(e);
			return new PullRequestResponse(e.getMessage(), null);
		}
	}

	@GetMapping("/pr-sync/{owner}/{repository}/{prId}")
	public PullRequestResponse analyzePullRequestDebug(@PathVariable String owner, @PathVariable String repository, @PathVariable Integer prId,
		HttpServletResponse response) {
		try {
			MaracasReport report = github.analyzePRSync(owner, repository, prId);
			return new PullRequestResponse("ok", report);
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			logger.error(e);
			return new PullRequestResponse(e.getMessage(), null);
		} catch (CloneException | BuildException | CompletionException e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.error(e);
			return new PullRequestResponse(e.getMessage(), null);
		}
	}
}
