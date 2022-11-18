package com.github.maracas.rest.controllers;

import com.github.maracas.rest.data.BreakingChangeDto;
import com.github.maracas.rest.data.BrokenUseDto;
import com.github.maracas.rest.data.ClientReport;
import com.github.maracas.rest.data.DeltaDto;
import com.github.maracas.rest.data.PackageReport;
import com.github.maracas.rest.data.PullRequestResponse;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
			File reportFile = Path.of("/mfnt/sion/Research/TSE/reports/breakbot-playground/checkstyle/1-816426688f3e2bcddf4a8692c381dc05d591d747.json").toFile();
			//File reportFile = Path.of("/home/dig/repositories/breakbot/test/fixtures/maracas/maracas.two-clients.json").toFile();

			PullRequestResponse report = PullRequestResponse.fromJson(reportFile);
			model.addAttribute("pr", report.pr());
			model.addAttribute("repoUrl", String.format("https://github.com/%s/%s", report.pr().owner(), report.pr().name()));
			model.addAttribute("generated", report.date().format(DateTimeFormatter.ofPattern("MMM d, y H:mm")));

			List<PackageReport> allPkgs = report.report().reports();
			List<BreakingChangeDto> allBcs = allPkgs.stream().map(PackageReport::delta).map(DeltaDto::breakingChanges).flatMap(Collection::stream).toList();
			List<BrokenUseDto> allBus = allPkgs.stream().map(PackageReport::allBrokenUses).flatMap(Collection::stream).toList();
			List<BreakingChangeDto> impactfulBcs = allPkgs.stream().map(PackageReport::impactfulBreakingChanges).flatMap(Collection::stream).toList();
			List<ClientReport> allClients = allPkgs.stream().map(PackageReport::clientReports).flatMap(Collection::stream).toList();
			List<ClientReport> brokenClients = allClients.stream().filter(c -> !c.brokenUses().isEmpty()).toList();
			List<ClientReport> saneClients = allClients.stream().filter(c -> c.brokenUses().isEmpty()).toList();

			Multimap<BreakingChangeDto, ClientReport> impactedClients = ArrayListMultimap.create();
			for (BreakingChangeDto bc : allBcs) {
				List<ClientReport> bcClients = allClients.stream().filter(c ->
					c.brokenUses().stream().anyMatch(bu -> bu.src().equals(bc.declaration()))).toList();
				impactedClients.putAll(bc, bcClients);
			}

			impactedClients.forEach((bc, c) -> {
				System.out.println(bc.declaration() + " impacts " + c.url());
			});

			model.addAttribute("pkgs", allPkgs);
			model.addAttribute("allBcs", allBcs);
			model.addAttribute("impactfulBcs", impactfulBcs);
			model.addAttribute("allBus", allBus);
			model.addAttribute("allClients", allClients);
			model.addAttribute("brokenClients", brokenClients);
			model.addAttribute("saneClients", saneClients);
			model.addAttribute("impactedClients", impactedClients);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "report";
	}
}
