package org.swat.maracas.spoon;

import java.util.List;

import japicmp.model.JApiClass;

public class Delta {
	private final List<JApiClass> classes;

	public Delta(List<JApiClass> classes) {
		this.classes = classes;
	}

	public List<JApiClass> getClasses() {
		return classes;
	}
}
