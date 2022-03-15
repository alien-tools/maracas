package com.github.maracas;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.maracas.brokenUse.BrokenUse;
import com.google.common.base.Stopwatch;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
	name        = "Maracas",
	description = "Compute changes between two library versions and their impact on client code",
	version     = "0.1.0"
)
public class MaracasCLI implements Runnable {
	@Option(names = {"-o", "--old"}, required = true,
		description = "The library's old JAR")
	private Path v1;

	@Option(names = {"-n", "--new"}, required = true,
		description = "The library's new JAR")
	private Path v2;

	@Option(names = {"-c", "--client"}, arity="0..*",
		description = "Directory containing the client's source code")
	private final List<Path> clients = new ArrayList<>();

	@Option(names = {"-s", "--sources"},
		description = "Directory containing the old library's source code")
	private Path sources;

	@Override
	public void run() {
		try {
			Stopwatch watch = Stopwatch.createStarted();
			AnalysisQuery query = AnalysisQuery.builder()
				.oldJar(v1)
				.newJar(v2)
				.clients(clients)
				.build();
			AnalysisResult result = Maracas.analyze(query);

			if (sources != null)
				result.delta().populateLocations(sources);

			System.out.println("""
			+------------------+
			+ BREAKING CHANGES +
			+------------------+
			""");
			System.out.println(result.delta());

			System.out.println("""
			+------------+
			+ BROKEN USES +
			+------------+
			""");
			System.out.println(
				result.allBrokenUses()
					.stream()
					.map(BrokenUse::toString)
					.collect(Collectors.joining("\n"))
			);
			System.out.println("Done in " + watch.elapsed(TimeUnit.SECONDS) + "s.");
		} catch (Exception e) {
			System.err.println("Fatal error: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new MaracasCLI()).execute(args);
		System.exit(exitCode);
	}
}
