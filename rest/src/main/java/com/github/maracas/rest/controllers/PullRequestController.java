package com.github.maracas.rest.controllers;

import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.build.BuildException;
import com.github.maracas.forges.clone.CloneException;
import com.github.maracas.rest.data.MaracasReport;
import com.github.maracas.rest.data.PullRequestResponse;
import com.github.maracas.rest.services.BreakbotService;
import com.github.maracas.rest.services.PullRequestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
public class PullRequestController {
	private final PullRequestService prService;
	private final BreakbotService breakbotService;

	private static final Logger logger = LogManager.getLogger(PullRequestController.class);

	public PullRequestController(PullRequestService prService, BreakbotService breakbotService) {
		this.prService = prService;
		this.breakbotService = breakbotService;
	}

	@PostMapping("/pr/{owner}/{name}/{number}")
	public ResponseEntity<PullRequestResponse> analyzePullRequest(
		@PathVariable String owner,
		@PathVariable String name,
		@PathVariable Integer number,
		@RequestParam(required=false) String callback,
		@RequestHeader(required=false) String installationId
	) {
		try {
			PullRequest pr = prService.fetchPullRequest(owner, name, number);
			String location = prService.analyzePR(pr, callback, installationId);
			return ResponseEntity
				.accepted()
				.header("Location", location)
				.body(PullRequestResponse.status(pr, "processing"));
		} catch (Throwable t) {
			// We *always* need to invoke the callback (if any) with the results, errors included.
			// We rethrow the exception to let the exception handlers return the proper message.
			if (callback != null)
				breakbotService.sendPullRequestResponse(PullRequestResponse.status(null, t.getMessage()), callback, installationId);
			throw t;
		}
	}

	@GetMapping("/pr/{owner}/{name}/{number}")
	public ResponseEntity<PullRequestResponse> getPullRequest(
		@PathVariable String owner,
		@PathVariable String name,
		@PathVariable Integer number
	) {
		PullRequest pr = prService.fetchPullRequest(owner, name, number);

		// Either we have it already
		PullRequestResponse response = prService.readResponse(pr);
		if (response != null) {
			return ResponseEntity.ok(response);
		}

		// Or we're currently computing it
		if (prService.isProcessing(pr)) {
			return ResponseEntity
				.status(HttpStatus.PROCESSING)
				.body(PullRequestResponse.status(pr, "processing"));
		}

		// Or it doesn't exist
		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(PullRequestResponse.status(pr, "This PR isn't being analyzed"));
	}

	@PostMapping("/pr-sync/{owner}/{name}/{number}")
	public ResponseEntity<PullRequestResponse> analyzePullRequestSync(
		@PathVariable String owner,
		@PathVariable String name,
		@PathVariable Integer number
	) {
		PullRequest pr = prService.fetchPullRequest(owner, name, number);
		MaracasReport report = prService.analyzePRSync(pr);
		return ResponseEntity.ok(PullRequestResponse.ok(pr, report));
	}

	@ExceptionHandler({BuildException.class, CloneException.class})
	public ResponseEntity<PullRequestResponse> handleInternalException(BuildException e) {
		logger.error(e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(PullRequestResponse.status(null, e.getMessage()));
	}

	@ExceptionHandler({ForgeException.class})
	public ResponseEntity<PullRequestResponse> handleGitHubException(Exception e) {
		logger.error(e);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(PullRequestResponse.status(null, e.getMessage()));
	}

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<PullRequestResponse> handleThrowable(Throwable t) {
		logger.error("Uncaught throwable", t);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(PullRequestResponse.status(null, t.getMessage()));
	}
}
