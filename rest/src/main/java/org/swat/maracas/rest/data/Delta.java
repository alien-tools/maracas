package org.swat.maracas.rest.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Delta {
	private Path jarV1;
	private Path jarV2;
	private List<BreakingChangeInstance> breakingChanges = new ArrayList<>();

	private Delta() {

	}

	public static Delta fromMaracasDelta(org.swat.maracas.spoon.Delta d) {
		return new Delta();
	}

	public Path getJarV1() {
		return jarV1;
	}

	public Path getJarV2() {
		return jarV2;
	}

	public void setJarV1(Path jarV1) {
		this.jarV1 = jarV1;
	}

	public void setJarV2(Path jarV2) {
		this.jarV2 = jarV2;
	}

	public List<BreakingChangeInstance> getBreakingChanges() {
		return breakingChanges;
	}
}
