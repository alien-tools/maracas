package org.swat.maracas.spoon.tests.compchanges;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.swat.maracas.spoon.Delta;
import org.swat.maracas.spoon.Detection;
import org.swat.maracas.spoon.visitors.ImpactVisitor;

import japicmp.model.JApiClass;
import japicmp.output.Filter;
import spoon.Launcher;
import spoon.reflect.CtModel;

public class CompChanges {
	public static Set<Detection> computeDetections() {
		Path v1 = Paths.get("/home/dig/repositories/maracas/data/comp-changes-old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/maracas/data/comp-changes-new/target/comp-changes-0.0.2.jar");
		Path client = Paths.get("/home/dig/repositories/comp-changes-client/src/");

		Delta delta = new Delta();
		List<JApiClass> classes = delta.compute(v1, v2, Collections.emptyList(), Collections.emptyList());

		Launcher launcher = new Launcher();
		launcher.addInputResource(client.toAbsolutePath().toString());
		String[] cp = {v1.toAbsolutePath().toString()};
		launcher.getEnvironment().setSourceClasspath(cp);
		CtModel model = launcher.buildModel();

		ImpactVisitor visitor = new ImpactVisitor(model.getRootPackage());
		Filter.filter(classes, visitor);

		return visitor.getDetections();
	}
}
