package com.github.maracas.forges.report;

import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Package;
import com.github.maracas.forges.PullRequest;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public record PackageReport(
	Package pkg,
	ForgeDelta delta,
	List<ClientImpact> clientsImpact,
	String error
) {
	public static PackageReport error(Package pkg, String error) {
		return new PackageReport(pkg, null, Collections.emptyList(), error);
	}

	public static PackageReport success(Package pkg, Delta delta, List<ClientImpact> clientsImpact, PullRequest pr, Path clone) {
		return new PackageReport(pkg, ForgeDelta.of(delta, pr, clone), clientsImpact, null);
	}

	public List<ForgeBrokenUse> allBrokenUses() {
		return clientsImpact.stream()
			.map(ClientImpact::brokenUses)
			.flatMap(Collection::stream)
			.toList();
	}
}
