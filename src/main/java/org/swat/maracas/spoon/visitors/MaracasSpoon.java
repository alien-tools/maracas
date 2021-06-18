package org.swat.maracas.spoon.visitors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.swat.maracas.spoon.Delta;
import org.swat.maracas.spoon.SpoonHelper;

import japicmp.model.JApiClass;
import japicmp.model.JApiCompatibilityChange;
import japicmp.output.Filter;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.declaration.CtElement;

public class MaracasSpoon {
	public static void main(String[] args) {
		Path v1 = Paths.get("/home/dig/repositories/maracas/data/comp-changes-old/target/comp-changes-0.0.1.jar");
		Path v2 = Paths.get("/home/dig/repositories/maracas/data/comp-changes-new/target/comp-changes-0.0.2.jar");
		Path c = Paths.get("/home/dig/repositories/maracas/data/comp-changes-client/src/");
		//Path c = Paths.get("/home/dig/repositories/maracas/data/comp-changes-client/src/mainclient/annotationDeprecatedAdded/AnnotationDeprecatedAddedExt.java");
		new MaracasSpoon().run(v1, v2, c);
	}

	public void run(Path v1, Path v2, Path client) {
		Delta delta = new Delta();
		List<JApiClass> classes = delta.compute(v1, v2, Collections.emptyList(), Collections.emptyList());

		Launcher launcher = new Launcher();
		launcher.addInputResource(client.toAbsolutePath().toString());
		String[] cp = {v1.toAbsolutePath().toString()};
		launcher.getEnvironment().setSourceClasspath(cp);
		CtModel model = launcher.buildModel();

		ImpactVisitor visitor = new ImpactVisitor(model.getRootPackage());
		Filter.filter(classes, visitor);

		visitor.getDetections().forEach(d -> {
			if (d.getChange() == JApiCompatibilityChange.CLASS_LESS_ACCESSIBLE)
				System.out.println(d);
		});

		visitor.getDetections().forEach(d -> {
			CtElement anchor = SpoonHelper.firstLocatableParent(d.getElement());
			
			if (anchor != null)
				anchor.addComment(model.getRootPackage().getFactory().Code().createComment(d.toJavaComment(), CommentType.INLINE));
			else
				System.out.println("Cannot attach comment on " + d);
		});
		
		launcher.setSourceOutputDirectory("/home/dig/repositories/comp-changes-data/client-commented/src");
		launcher.prettyprint();
	}
}
