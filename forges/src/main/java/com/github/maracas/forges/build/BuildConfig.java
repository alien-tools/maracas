package com.github.maracas.forges.build;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class BuildConfig {
	private final Path module;
	private final List<String> goals = new ArrayList<>();
	private final Properties properties = new Properties();

	public static BuildConfig newDefault() {
		return new BuildConfig(Path.of(""));
	}

	public BuildConfig(Path module) {
		Objects.requireNonNull(module);
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
