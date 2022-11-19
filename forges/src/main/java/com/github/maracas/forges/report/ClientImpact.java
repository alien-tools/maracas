package com.github.maracas.forges.report;

import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.forges.Commit;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public record ClientImpact(
	Commit client,
	List<ForgeBrokenUse> brokenUses,
	String error
) {
	public static ClientImpact success(Commit client, DeltaImpact impact, Path clone) {
		return new ClientImpact(
			client,
			impact.getBrokenUses().stream()
				.map(bu -> ForgeBrokenUse.of(bu, client.repository(), clone))
				.toList(),
			impact.getThrowable() != null ? impact.getThrowable().getMessage() : null
		);
	}

	public static ClientImpact noImpact(Commit client) {
		return new ClientImpact(
			client,
			Collections.emptyList(),
			null
		);
	}

	public static ClientImpact error(Commit client, String error) {
		return new ClientImpact(
			client,
			Collections.emptyList(),
			error
		);
	}
}
