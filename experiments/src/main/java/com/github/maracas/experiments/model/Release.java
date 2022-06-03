package com.github.maracas.experiments.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Release {
	private final String version;
	private final LocalDate date; //updatedAt
	private final Repository repository;
	private List<Package> packages;

	public Release(String version, LocalDate date, Repository repository) {
		this.version = version;
		this.date = date;
		this.repository = repository;
		this.packages = new ArrayList<Package>();
	}

	public List<Package> getPackages() {
		return packages;
	}

	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}

	public void addPackage(Package pkg) {
		if (pkg != null)
			packages.add(pkg);
	}

	public String getVersion() {
		return version;
	}

	public LocalDate getDate() {
		return date;
	}

	public Repository getRepository() {
		return repository;
	}
}
