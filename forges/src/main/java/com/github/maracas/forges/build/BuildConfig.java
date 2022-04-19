package com.github.maracas.forges.build;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class BuildConfig {
	private final Path basePath;
	private final Path module;
	private final List<String> goals = new ArrayList<>();
	private final Properties properties = new Properties();

	public BuildConfig(Path basePath) {
		Objects.requireNonNull(basePath);
		this.basePath = basePath;
		this.module = Paths.get("");
	}

	public BuildConfig(Path basePath, Path module) {
		Objects.requireNonNull(basePath);
		Objects.requireNonNull(module);
		this.basePath = basePath;
		this.module = module;
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

	public Path getModule() {
		return module;
	}

	public List<String> getGoals() {
		return goals;
	}

	public Properties getProperties() {
		return properties;
	}
}
