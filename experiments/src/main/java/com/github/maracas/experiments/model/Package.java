package com.github.maracas.experiments.model;

public class Package {
	private final String name;
	private final Repository repository;
	private String srcUrl;

	public enum PackageSourceType {
		UNDEFINED
	}

	public Package(String name, Repository repository) {
		this.name = name;
		this.repository = repository;
		this.srcUrl = PackageSourceType.UNDEFINED.toString();
	}

	public String getName() {
		return name;
	}

	public Repository getRepository() {
		return repository;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}
}
