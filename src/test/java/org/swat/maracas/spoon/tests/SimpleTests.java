package org.swat.maracas.spoon.tests;

import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.swat.maracas.spoon.TryingStuff;

import spoon.Launcher;
import spoon.reflect.CtModel;

public class SimpleTests {
	private CtModel model;

	@Before
	public void setUp() {
		Launcher launcher = new Launcher();
		launcher.addInputResource("/home/dig/repositories/maracas/data/comp-changes-client/src/");
		String[] cp = {"/home/dig/repositories/maracas/data/comp-changes-old/target/comp-changes-0.0.1.jar"};
		launcher.getEnvironment().setSourceClasspath(cp);
		model = launcher.buildModel();
	}

	@Test
	public void test() {
		System.out.println(
			TryingStuff.findFieldWrite(model, "main.fieldNowFinal.FieldNowFinal", "fieldFinal")
				.stream().map(f -> f.getPosition()).collect(Collectors.toList()));
	}

}
