package com.github.maracas.rest.controllers;

import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import com.github.maracas.rest.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
public class PullRequestController {
	@Autowired
	private PullRequestService prService;
	@Autowired
	private BreakbotService breakbotService;

	private static final Logger logger = LogManager.getLogger(PullRequestController.class);

	@PostMapping("/pr/{owner}/{repository}/{prId}")
	public ResponseEntity<PullRequestResponse> analyzePullRequest(
		@PathVariable String owner,
		@PathVariable String repository,
		@PathVariable Integer prId,
		@RequestParam(required=false) String callback,
		@RequestHeader(required=false) String installationId,
		@RequestBody(required=false) String breakbotYaml
	) {
		try {
			String location = prService.analyzePR(owner, repository, prId, callback, installationId, breakbotYaml);
			return ResponseEntity
				.accepted()
				.header("Location", location)
				.body(new PullRequestResponse("processing"));
		} catch (Throwable t) {
			// We *always* need to invoke the callback (if any) with the results, errors included.
			// We rethrow the exception to let the exception handlers return the proper message.
			if (callback != null)
				breakbotService.sendPullRequestResponse(new PullRequestResponse(t.getMessage()), callback, installationId);
			throw t;
		}
	}

	@GetMapping("/pr/{owner}/{repository}/{prId}")
	public ResponseEntity<PullRequestResponse> getPullRequest(
		@PathVariable String owner,
		@PathVariable String repository,
		@PathVariable Integer prId
	) {
		// Either we have it already
		MaracasReport report = prService.getReport(owner, repository, prId);
		if (report != null) {
			return ResponseEntity.ok(new PullRequestResponse("ok", report));
		}

		// Or we're currently computing it
		if (prService.isProcessing(owner, repository, prId)) {
			return ResponseEntity
				.status(HttpStatus.PROCESSING)
				.body(new PullRequestResponse("processing"));
		}

		// Or it doesn't exist
		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(new PullRequestResponse("This PR isn't being analyzed"));
	}

	@PostMapping("/pr-sync/{owner}/{repository}/{prId}")
	public ResponseEntity<PullRequestResponse> analyzePullRequestSync(
		@PathVariable String owner,
		@PathVariable String repository,
		@PathVariable Integer prId,
		@RequestBody(required=false) String breakbotYaml
	) {
		MaracasReport report = prService.analyzePRSync(owner, repository, prId, breakbotYaml);
		return ResponseEntity.ok(new PullRequestResponse("ok", report));
	}

	@ExceptionHandler({BuildException.class, CloneException.class, MaracasException.class})
	public ResponseEntity<PullRequestResponse> handleInternalExceptions(BuildException e) {
		logger.error(e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new PullRequestResponse(e.getMessage()));
	}

	@ExceptionHandler(GitHubException.class)
	public ResponseEntity<PullRequestResponse> handleGitHubExceptions(GitHubException e) {
		logger.error(e);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new PullRequestResponse(e.getMessage()));
	}

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<PullRequestResponse> handleThrowables(Throwable t) {
		logger.error("Uncaught throwable", t);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new PullRequestResponse(t.getMessage()));
	}
}
