package org.swat.maracas.rest.impact;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GHRepository;
import org.swat.maracas.rest.MaracasService;
import org.swat.maracas.rest.breakbot.BreakbotConfig;
import org.swat.maracas.rest.data.Delta;
import org.swat.maracas.rest.data.Detection;
import org.swat.maracas.rest.data.ImpactModel;
import org.swat.maracas.rest.tasks.CloneAndBuild;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;

public class GithubRepository implements Impactable {
	private final MaracasService maracas;
	private final BreakbotConfig config;
	private final GHRepository repository;
	private final String clonePath;
	private static final Logger logger = LogManager.getLogger(GithubRepository.class);

	public GithubRepository(MaracasService maracas, BreakbotConfig config, GHRepository repository, String clonePath) {
		this.maracas = maracas;
		this.config = config;
		this.repository = repository;
		this.clonePath = clonePath;
	}

	@Override
	public ImpactModel computeImpact(Delta delta) {
		try {
			Path clientPath = Paths.get(clonePath)
				.resolve(repository.getOwnerName())
				.resolve(repository.getBranch(repository.getDefaultBranch()).getSHA1());

			// Clone and build the client
			Path clientJar = new CloneAndBuild(repository.getHttpTransportUrl(), repository.getDefaultBranch(),
				clientPath, config).get();

			// Build impact model
			// FIXME: we should reuse the existing delta model,
			// but easier for now to just pass all the JARs to Maracas
			IList detections = maracas.computeImpact(delta.getJarV1(), delta.getJarV2(), clientJar, clientPath);
			ImpactModel impact = new ImpactModel();
			impact.setClientJar(clientJar);
			impact.setClientUrl(repository.getHtmlUrl().toString());

			detections.forEach(rascalDetection -> {
				Detection d = Detection.fromRascal((IConstructor) rascalDetection);
				d.setClientUrl(repository.getHtmlUrl().toString());
				d.setPath(d.getPath().replaceFirst(clientPath.toAbsolutePath().toString(), ""));
				d.setUrl(
					String.format("%s/blob/%s/%s#L%s-L%s",
						repository.getHtmlUrl(), repository.getDefaultBranch(), d.getPath(),
						d.getStartLine(), d.getEndLine())
				);

				impact.addDetection(d);
			});

			return impact;
		} catch (IOException e) {
			logger.error(e);
			return new ImpactModel(e);
		}
	}
}
