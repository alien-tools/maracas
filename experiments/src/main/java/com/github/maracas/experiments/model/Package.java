package com.github.maracas.experiments.model;

public class Package {
	private final String name;
	private final Repository repository;
	private Release release;
	private String srcUrl;

	public enum PackageSourceType {
		UNDEFINED
	}

	public Package(String name, Repository repository) {
		this.name = name;
		this.repository = repository;
		this.srcUrl = PackageSourceType.UNDEFINED.toString();
	}

	public Release getRelease() {
		return release;
	}

	public void setRelease(Release release) {
		this.release = release;
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
