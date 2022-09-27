package com.github.maracas.rest.controllers;

import com.github.maracas.rest.data.BreakingChange;
import com.github.maracas.rest.data.PullRequestResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
public class ReportController {
	@GetMapping("/report/{owner}/{name}/{prNumber}")
	public String viewReport(
		@PathVariable String owner,
		@PathVariable String name,
		@PathVariable Integer prNumber,
		Model model
	) {
		try {
			File reportFile = Path.of("/home/dig/repositories/maracas/rest/src/test/resources/spoon-report.json").toFile();

			PullRequestResponse report = PullRequestResponse.fromJson(reportFile);
			model.addAttribute("owner", owner);
			model.addAttribute("name", name);
			model.addAttribute("prNumber", prNumber);
			model.addAttribute("generated", new Date(reportFile.lastModified()));
			model.addAttribute("prUrl", String.format("https://github.com/%s/%s/pulls/%d", owner, name, prNumber));

			model.addAttribute("report", report.report());
			model.addAttribute("delta", report.report().delta());
			model.addAttribute("impact", report.report().clientReports());
			model.addAttribute("bcs", report.report().delta().breakingChanges());

			model.addAttribute("dummyUrl", "https://www.google.com");

			return "report";
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "report";
	}
}
