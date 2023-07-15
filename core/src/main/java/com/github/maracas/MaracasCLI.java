package com.github.maracas;

import com.github.maracas.brokenuse.BrokenUse;
import com.google.common.base.Stopwatch;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Command(
	name = "Maracas",
	description = "Compute changes between two library versions and their impact on client code",
	version = "0.1.0"
)
public class MaracasCLI implements Runnable {
	@Option(names = {"-o", "--old"}, required = true,
		description = "The library's old JAR")
	private Path v1;

	@Option(names = {"-n", "--new"}, required = true,
		description = "The library's new JAR")
	private Path v2;

	@Option(names = {"-c", "--client"}, arity = "0..*",
		description = "Directory containing the client's source code")
	private final List<Path> clientPaths = new ArrayList<>();

	@Option(names = {"-s", "--sources"},
		description = "Directory containing the old library's source code")
	private Path sources;

	@Override
	public void run() {
		try {
			Stopwatch watch = Stopwatch.createStarted();
			LibraryJar oldVersion = sources != null
				? LibraryJar.withSources(v1, SourcesDirectory.of(sources))
				: LibraryJar.withoutSources(v1);
			LibraryJar newVersion = LibraryJar.withoutSources(v2);
			List<SourcesDirectory> clients = clientPaths.stream().map(SourcesDirectory::of).toList();
			AnalysisQuery query = AnalysisQuery.builder()
				.of(oldVersion, newVersion)
				.clients(clients)
				.build();
			AnalysisResult result = new Maracas().analyze(query);

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
