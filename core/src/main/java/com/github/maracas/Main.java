package com.github.maracas;

import com.github.maracas.delta.Delta;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) {
		Path x = Paths.get("/home/dig/v1.jar");
		Path y = Paths.get("/home/dig/v2.jar");
		Delta delta = Maracas.computeDelta(x, y);
		delta.populateLocations(Paths.get("/home/dig/sources/src/main/java"));
		System.out.println(delta);
	}
}
