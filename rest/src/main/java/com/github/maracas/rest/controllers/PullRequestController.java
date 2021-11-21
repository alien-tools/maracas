package com.github.maracas.rest.controllers;

import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import com.github.maracas.rest.services.PullRequestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
		} catch (Throwable t) {
			logger.error(t);
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new PullRequestResponse(t.getMessage()));
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
		} catch (Throwable t) {
			logger.error(t);
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new PullRequestResponse(t.getMessage()));
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
		} catch (Throwable t) {
			logger.error(t);
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new PullRequestResponse(t.getMessage()));
		}
	}
}
