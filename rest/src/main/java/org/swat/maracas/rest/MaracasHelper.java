package org.swat.maracas.rest;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rascalmpl.interpreter.ConsoleRascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.util.ConcurrentSoftReferenceObjectPool;
import org.rascalmpl.values.ValueFactoryFactory;

import com.google.common.collect.ImmutableMap;

import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class MaracasHelper {
	private static MaracasHelper instance;
	private final ConcurrentSoftReferenceObjectPool<Evaluator> pool = getEvaluatorPool();
	private static final Logger logger = LogManager.getLogger(MaracasHelper.class);

	private MaracasHelper() {}

	public synchronized static MaracasHelper getInstance() {
	    if (instance == null)
	    	instance = new MaracasHelper();
	    return instance;
	}

	public IValue computeDelta(Path oldJar, Path newJar) {
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		return run((eval) -> {
			return eval.call("compareJars", "org::maracas::delta::JApiCmp",
				ImmutableMap.of("oldCP", vf.list(), "newCP", vf.list()),
				vf.sourceLocation(oldJar.toAbsolutePath().toString()), vf.sourceLocation(newJar.toAbsolutePath().toString()),
				vf.string("v1"), vf.string("v2"));
		});
	}

	public ISet getChangedEntities(IValue delta) {
		return (ISet) run((eval) -> eval.call("getChangedEntities", delta));
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

		eval.doImport(new ConsoleRascalMonitor(), "org::maracas::delta::JApiCmp");
		eval.doImport(new ConsoleRascalMonitor(), "org::maracas::delta::JApiCmpDetector");
		eval.doImport(new ConsoleRascalMonitor(), "org::maracas::measure::delta::Evolution");
		eval.doImport(new ConsoleRascalMonitor(), "org::maracas::measure::delta::Impact");
		eval.doImport(new ConsoleRascalMonitor(), "org::maracas::m3::Core");
		eval.doImport(new ConsoleRascalMonitor(), "lang::java::m3::Core");
		eval.doImport(new ConsoleRascalMonitor(), "lang::json::IO");

		return eval;
	}
}
