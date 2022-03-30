package com.github.maracas.forges.build;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class BuildConfig {
	private final Path basePath;
	private final List<String> goals = new ArrayList<>();
	private final Properties properties = new Properties();

	public BuildConfig(Path basePath) {
		Objects.requireNonNull(basePath);
		this.basePath = basePath;
	}

	public void addGoal(String goal) {
		Objects.requireNonNull(goal);
		goals.add(goal);
	}

	public void setProperty(String name, String value) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		properties.setProperty(name, value);
	}

	public Path getBasePath() {
		return basePath;
	}

	public List<String> getGoals() {
		return goals;
	}

	public Properties getProperties() {
		return properties;
	}
}
