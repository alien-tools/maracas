package com.github.maracas.forges.report;

import com.github.maracas.delta.Delta;
import com.github.maracas.forges.Commit;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public record CommitsReport(
	Commit v1,
	Commit v2,
	ForgeDelta delta,
	List<ClientImpact> clientsImpact,
	String error
) {
	public static CommitsReport success(Commit v1, Commit v2, Delta delta, List<ClientImpact> clientsImpact, Path clone) {
		return new CommitsReport(
			v1,
			v2,
			ForgeDelta.of(delta, v1, v2, clone),
			clientsImpact,
			null
		);
	}

	public static CommitsReport error(Commit v1, Commit v2, String error) {
		return new CommitsReport(v1,
			v2,
			null,
			Collections.emptyList(),
			error
		);
	}

	public static CommitsReport noImpact(Commit v1, Commit v2, Delta delta, Path clone) {
		return new CommitsReport(
			v1,
			v2,
			ForgeDelta.of(delta, v1, v2, clone),
			Collections.emptyList(),
			null
		);
	}
}
