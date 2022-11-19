package com.github.maracas.rest.controllers;

import com.github.maracas.rest.data.BreakingChangeDto;
import com.github.maracas.rest.data.BrokenUseDto;
import com.github.maracas.rest.data.ClientImpactDto;
import com.github.maracas.rest.data.DeltaDto;
import com.github.maracas.rest.data.PackageReportDto;
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
			//File reportFile = Path.of("/mnt/sion/Research/TSE/reports/breakbot-playground/checkstyle/1-816426688f3e2bcddf4a8692c381dc05d591d747.json").toFile();
			File responseFile = Path.of("maracas.report.json").toFile();

			PullRequestResponse response = PullRequestResponse.fromJson(responseFile);
			model.addAttribute("pr", response.pr());
			model.addAttribute("repoUrl", String.format("https://github.com/%s/%s", response.pr().owner(), response.pr().name()));
			model.addAttribute("generated", response.date().format(DateTimeFormatter.ofPattern("MMM d, y H:mm")));

			List<PackageReportDto> allPkgs = response.report().packageReports().stream()
				.filter(pkg -> pkg.delta() != null)
				.toList();
			List<BreakingChangeDto> allBcs = allPkgs.stream()
				.map(PackageReportDto::delta)
				.map(DeltaDto::breakingChanges)
				.flatMap(Collection::stream)
				.toList();
			List<BrokenUseDto> allBus = allPkgs.stream()
				.map(PackageReportDto::allBrokenUses)
				.flatMap(Collection::stream)
				.toList();
			List<BreakingChangeDto> impactfulBcs = allPkgs.stream()
				.map(PackageReportDto::impactfulBreakingChanges)
				.flatMap(Collection::stream)
				.toList();
			List<ClientImpactDto> allClients = allPkgs.stream()
				.map(PackageReportDto::clientReports)
				.flatMap(Collection::stream)
				.toList();
			List<ClientImpactDto> brokenClients = allClients.stream()
				.filter(c -> !c.brokenUses().isEmpty())
				.toList();
			List<ClientImpactDto> saneClients = allClients.stream()
				.filter(c -> c.brokenUses().isEmpty())
				.toList();

			Multimap<BreakingChangeDto, ClientImpactDto> impactedClients = ArrayListMultimap.create();
			for (BreakingChangeDto bc : allBcs) {
				List<ClientImpactDto> bcClients = allClients.stream().filter(c ->
					c.brokenUses().stream().anyMatch(bu -> bu.src().equals(bc.declaration()))).toList();
				impactedClients.putAll(bc, bcClients);
			}

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
