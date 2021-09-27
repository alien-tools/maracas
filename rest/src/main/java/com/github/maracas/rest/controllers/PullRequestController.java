package com.github.maracas.rest.controllers;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import com.github.maracas.rest.services.BuildException;
import com.github.maracas.rest.services.CloneException;
import com.github.maracas.rest.services.PullRequestService;

@RestController
@RequestMapping("/github")
public class PullRequestController {
	@Autowired
	private PullRequestService github;

	private static final Logger logger = LogManager.getLogger(PullRequestController.class);

	@PostMapping("/pr/{owner}/{repository}/{prId}")
	public ResponseEntity<PullRequestResponse> analyzePullRequest(
		@PathVariable String owner,
		@PathVariable String repository,
		@PathVariable Integer prId,
		@RequestParam(required=false) String callback,
		@RequestHeader(required=false) String installationId)
	{
		try {
			String location = github.analyzePR(owner, repository, prId, callback, installationId);
			return ResponseEntity
				.accepted()
				.header("Location", location)
				.body(new PullRequestResponse("processing"));
		} catch (IOException e) {
			logger.error(e);
			return ResponseEntity
				.badRequest()
				.body(new PullRequestResponse(e.getMessage()));
		} catch (Exception e) {
			logger.error(e);
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new PullRequestResponse(e.getMessage()));
		}
	}

	@GetMapping("/pr/{owner}/{repository}/{prId}")
	public ResponseEntity<PullRequestResponse> getPullRequest(
		@PathVariable String owner,
		@PathVariable String repository,
		@PathVariable Integer prId
	) {
		try {
			// Either we have it already
			MaracasReport report = github.getReport(owner, repository, prId);
			if (report != null) {
				return ResponseEntity.ok(new PullRequestResponse("ok", report));
			}

			// Or we're currently computing it
			if (github.isProcessing(owner, repository, prId)) {
				return ResponseEntity
					.status(HttpStatus.PROCESSING)
					.body(new PullRequestResponse("processing"));
			}

			// Or it doesn't exist
			return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(new PullRequestResponse("This PR isn't being analyzed"));
		} catch (IOException e) {
			logger.error(e);
			return ResponseEntity
				.badRequest()
				.body(new PullRequestResponse(e.getMessage()));
		} catch (Exception e) {
			logger.error(e);
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new PullRequestResponse(e.getMessage()));
		}
	}

	@PostMapping("/pr-sync/{owner}/{repository}/{prId}")
	public ResponseEntity<PullRequestResponse> analyzePullRequestDebug(
		@PathVariable String owner,
		@PathVariable String repository,
		@PathVariable Integer prId,
		@RequestBody(required=false) String breakbotYaml
	) {
		try {
			MaracasReport report = github.analyzePRSync(owner, repository, prId, breakbotYaml);
			return ResponseEntity.ok(new PullRequestResponse("ok", report));
		} catch (IOException e) {
			logger.error(e);
			return ResponseEntity
				.badRequest()
				.body(new PullRequestResponse(e.getMessage()));
		} catch (CloneException | BuildException | CompletionException e) {
			logger.error(e);
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new PullRequestResponse(e.getMessage()));
		}
	}
}
