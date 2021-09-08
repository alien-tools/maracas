package org.swat.maracas.spoon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.swat.maracas.spoon.delta.Delta;
import org.swat.maracas.spoon.delta.Detection;
import org.swat.maracas.spoon.visitors.BreakingChangeVisitor;
import org.swat.maracas.spoon.visitors.CombinedVisitor;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.declaration.CtElement;

public class ClientAnalyzer {
	private Delta delta;
	private final Path client;
	private final Path cp;
	private CtModel model;
	private List<Detection> detections = new ArrayList<>();
	private final Launcher launcher = new Launcher();

	public ClientAnalyzer(Delta delta, Path client, Path cp) {
		this.delta = delta;
		this.client = client;
		this.cp = cp;
	}

	public void computeDetections() {
		launcher.addInputResource(client.toAbsolutePath().toString());
		String[] javaCp = { cp.toAbsolutePath().toString() };
		launcher.getEnvironment().setSourceClasspath(javaCp);
		model = launcher.buildModel();

		List<BreakingChangeVisitor> visitors = delta.getVisitors();
		CombinedVisitor visitor = new CombinedVisitor(visitors);

		visitor.scan(model.getRootPackage());

		detections = visitor.getDetections();
	}

	public void writeAnnotatedClient(Path output) {
		detections.forEach(d -> {
			CtElement anchor = SpoonHelper.firstLocatableParent(d.element());
			String comment = String.format("[%s:%s]", d.change(), d.use());

			if (anchor != null)
				anchor.addComment(model.getRootPackage().getFactory().Code().createComment(comment, CommentType.BLOCK));
			else
				System.out.println("Cannot attach comment on " + d);
		});

		launcher.setSourceOutputDirectory(output.toFile());
		launcher.prettyprint();
	}
	public List<Detection> getDetections() {
		return detections;
	}
}
