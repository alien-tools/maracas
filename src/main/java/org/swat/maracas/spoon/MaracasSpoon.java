package org.swat.maracas.spoon;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import japicmp.model.JApiAnnotation;
import japicmp.model.JApiClass;
import japicmp.model.JApiConstructor;
import japicmp.model.JApiField;
import japicmp.model.JApiImplementedInterface;
import japicmp.model.JApiMethod;
import japicmp.model.JApiSuperclass;
import japicmp.output.Filter;
import japicmp.output.Filter.FilterVisitor;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.reference.CtTypeReference;

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

		List<CtTypeReference<?>> types = new ArrayList<>();
		Filter.filter(classes, new FilterVisitor() {
			@Override
			public void visit(Iterator<JApiClass> iterator, JApiClass jApiClass) {
				// TODO Auto-generated method stub
				types.add(model.getRootPackage().getFactory().Type().createReference(jApiClass.getFullyQualifiedName()));
			}

			@Override
			public void visit(Iterator<JApiMethod> iterator, JApiMethod jApiMethod) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(Iterator<JApiConstructor> iterator, JApiConstructor jApiConstructor) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(Iterator<JApiImplementedInterface> iterator,
					JApiImplementedInterface jApiImplementedInterface) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(Iterator<JApiField> iterator, JApiField jApiField) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(Iterator<JApiAnnotation> iterator, JApiAnnotation jApiAnnotation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void visit(JApiSuperclass jApiSuperclass) {
				// TODO Auto-generated method stub

			}
		});

//		LibraryUsageVisitor visit = new LibraryUsageVisitor(types);
//		visit.scan(model.getRootPackage());
		//model.getRootPackage().accept(visit);

//		ImpactProcessor processor = new ImpactProcessor(model);
//		//List<JApiClass> tst = Collections.singletonList(classes.get(3));
//		Filter.filter(classes, new ImpactVisitor(processor));
//		
//		System.out.println(processor.getDetections().size() + " detections found");
////		processor.getDetections().forEach(d -> System.out.println(d));
//
//		processor.getDetections().forEach(d -> {
//			String comment = "";
//			System.out.println(d);
//			d.getElement().addComment(model.getRootPackage().getFactory().Code().createComment(d.toString(), CommentType.BLOCK));
//		});
//		
//		launcher.setSourceOutputDirectory("/home/dig/repositories/comp-changes-client-output/src");
//		launcher.prettyprint();
	}
}
