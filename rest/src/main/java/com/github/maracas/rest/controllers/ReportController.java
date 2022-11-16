package com.github.maracas.rest.controllers;

import com.github.maracas.rest.data.BreakingChangeDto;
import com.github.maracas.rest.data.PackageReport;
import com.github.maracas.rest.data.PullRequestResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
			File reportFile = Path.of("/home/dig/repositories/breakbot/test/fixtures/maracas/maracas.two-clients.json").toFile();

			PullRequestResponse report = PullRequestResponse.fromJson(reportFile);
			model.addAttribute("pr", report.pr());
			model.addAttribute("generated", report.date().format(DateTimeFormatter.ofPattern("MMM d, Y H:mm")));

			PackageReport firstReport = report.report().reports().get(0);
			model.addAttribute("report", firstReport);
			model.addAttribute("delta", firstReport.delta());
			model.addAttribute("impact", firstReport.clientReports());
			model.addAttribute("bcs", firstReport.delta().breakingChanges());

			model.addAttribute("dummyUrl", "https://www.google.com");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "report";
	}
}
