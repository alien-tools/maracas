package org.swat.maracas.rest.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Delta {
	private Path jarV1;
	private Path jarV2;
	private Path sources;
	private List<BreakingChangeInstance> breakingChanges = new ArrayList<>();
	private Throwable error;

	private static final Logger logger = LogManager.getLogger(Delta.class);

	public Delta() {

	}

	public Delta(List<BreakingChangeInstance> breakingChanges) {
		this.breakingChanges = breakingChanges;
	}

	public Delta(Throwable error) {
		this.error = error;
	}

	public Path getJarV1() {
		return jarV1;
	}

	public Path getJarV2() {
		return jarV2;
	}

	public Path getSources() {
		return sources;
	}

	public void setJarV1(Path jarV1) {
		this.jarV1 = jarV1;
	}

	public void setJarV2(Path jarV2) {
		this.jarV2 = jarV2;
	}

	public void setSources(Path sources) {
		this.sources = sources;
	}

	public List<BreakingChangeInstance> getBreakingChanges() {
		return breakingChanges;
	}

	public Throwable getError() {
		return error;
	}

	public void weaveImpact(ImpactModel impact) {
		impact.getDetections().forEach(d -> {
			Optional<BreakingChangeInstance> bc =
				breakingChanges.stream()
				.filter(c -> c.getDeclaration().equals(d.getSrc()))
				.findFirst();

			if (bc.isPresent())
				bc.get().addDetection(d);
			else
				logger.warn("Couldn't find matching BC for {} => {}",
					d.getElem(), d.getUsed());
		});
	}

	public static Delta fromJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, Delta.class);
	}

	public String toJson() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(this);
	}

	public void writeJson(File json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(json, this);
	}
}
