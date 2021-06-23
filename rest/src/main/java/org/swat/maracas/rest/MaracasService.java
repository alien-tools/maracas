package org.swat.maracas.rest;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.util.ConcurrentSoftReferenceObjectPool;
import org.rascalmpl.values.ValueFactoryFactory;
import org.springframework.stereotype.Service;

import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

@Service
public class MaracasService {
	private final ConcurrentSoftReferenceObjectPool<Evaluator> pool = getEvaluatorPool();
	private static final Logger logger = LogManager.getLogger(MaracasService.class);

	public IList computeDelta(Path oldJar, Path newJar, Path sources) {
		logger.info("Computing delta ({} -> {})", oldJar, newJar);
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		return run(eval -> {
			Instant start = Instant.now();
			IList res = (IList) eval.call("bcInstances",
				vf.sourceLocation(oldJar.toAbsolutePath().toString()), vf.sourceLocation(newJar.toAbsolutePath().toString()),
				vf.sourceLocation(sources.toAbsolutePath().toString()));
			logger.info("Delta computation took {}ms", Duration.between(start, Instant.now()).toMillis());
			return res;
		});
	}

	public IList computeImpact(Path oldJar, Path newJar, Path clientJar, Path clientSources) {
		logger.info("Computing impact on {} ({} -> {})", clientJar, oldJar, newJar);
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		return run(eval -> {
			Instant start = Instant.now();
			IList res = (IList) eval.call("detections",
				vf.sourceLocation(oldJar.toAbsolutePath().toString()), vf.sourceLocation(newJar.toAbsolutePath().toString()),
				vf.sourceLocation(clientJar.toAbsolutePath().toString()),
				vf.sourceLocation(clientSources.toAbsolutePath().toString()));
			logger.info("Impact computation took {}ms", Duration.between(start, Instant.now()).toMillis());
			return res;
		});
	}

	public ISet getChangedEntities(IValue delta) {
		return (ISet) run(eval -> eval.call("getChangedEntities", delta));
	}

	public <T> T run(Function<Evaluator, T> func) {
		return pool.useAndReturn(func);
	}

	private ConcurrentSoftReferenceObjectPool<Evaluator> getEvaluatorPool() {
		return new ConcurrentSoftReferenceObjectPool<>(60, TimeUnit.MINUTES, 1, Runtime.getRuntime().availableProcessors(),
				() -> {
					logger.info("Building a fresh Rascal evaluator and adding it to the pool");
					return newEvaluator();
				});
	}

	private Evaluator newEvaluator() {
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment module = new ModuleEnvironment("$rascal-rest$", heap);
		Evaluator eval = new Evaluator(vf, System.in, System.err, System.out, module, heap);

		eval.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());
		ISourceLocation path = URIUtil.correctLocation("lib", "maracas", "");
		eval.addRascalSearchPath(path);

		eval.doImport(new NullRascalMonitor(), "org::maracas::delta::JApiCmp");
		eval.doImport(new NullRascalMonitor(), "org::maracas::delta::JApiCmpDetector");
		eval.doImport(new NullRascalMonitor(), "org::maracas::measure::delta::Evolution");
		eval.doImport(new NullRascalMonitor(), "org::maracas::measure::delta::Impact");
		eval.doImport(new NullRascalMonitor(), "org::maracas::m3::Core");
		eval.doImport(new NullRascalMonitor(), "lang::java::m3::Core");
		eval.doImport(new NullRascalMonitor(), "org::maracas::delta::rest::Rest");

		return eval;
	}
}
