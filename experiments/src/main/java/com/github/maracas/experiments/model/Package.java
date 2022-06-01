package com.github.maracas.experiments.model;

import java.util.ArrayList;
import java.util.List;

public class Package {
	private final String name;
	private final Repository repository;
	private Release release;
	private List<PackageFile> files;

	public Package(String name, Repository repository) {
		this.name = name;
		this.repository = repository;
		this.files = new ArrayList<PackageFile>();
	}

	public Release getRelease() {
		return release;
	}

	public void setRelease(Release release) {
		this.release = release;
	}

	public List<PackageFile> getFiles() {
		return files;
	}

	public void setFiles(List<PackageFile> files) {
		this.files = files;
	}

	public void addFile(PackageFile file) {
		if (file != null)
			files.add(file);
	}

	public String getName() {
		return name;
	}

	public Repository getRepository() {
		return repository;
	}
}
