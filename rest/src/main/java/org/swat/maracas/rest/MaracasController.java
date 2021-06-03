package org.swat.maracas.rest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.library.Prelude;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import io.usethesource.vallang.IRelation;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

@RestController
public class MaracasController {
	private static final String UPLOAD_PATH = "/home/dig/tmp";
	private static final String CLONE_PATH = "/home/dig/tmp/clones";
	
	private static final Logger logger = LogManager.getLogger("maracas-rest");

	// Considering the computation time, this should probably be a POST/GET duo
	@GetMapping("/github/detections/{user}/{repository}/{pr}")
	String delta(@PathVariable("user") String user, @PathVariable("repository") String repository, @PathVariable("pr") Integer prId) {
		try {
			// Retrieve PR metadata from GH
			GitHub gh = GitHubBuilder.fromPropertyFile().build();
			GHRepository repo = gh.getRepository(user + "/" + repository);
			GHPullRequest pullRequest = repo.getPullRequest(prId);
			GHCommitPointer head = pullRequest.getHead();
			GHCommitPointer base = pullRequest.getBase();
			String headSha = head.getSha();
			String baseSha = base.getSha();
			String headUrl = head.getRepository().getHttpTransportUrl();
			String baseUrl = base.getRepository().getHttpTransportUrl();
			String headRef = head.getRef();
			String baseRef = base.getRef();
			
			// Clone & build the BASE branch
			Path basePath = Paths.get(CLONE_PATH).resolve(baseSha);
			Optional<Path> v1 = cloneAndBuild(baseUrl, baseRef, basePath);

			// Clone & build the HEAD branch
			Path headPath = Paths.get(CLONE_PATH).resolve(headSha);
			Optional<Path> v2 = cloneAndBuild(headUrl, headRef, headPath);
			
			if (v1.isPresent() && v2.isPresent()) {
				Path j1 = v1.get();
				Path j2 = v2.get();
				
				Evaluator eval = createRascalEvaluator();
				IValueFactory vf = ValueFactoryFactory.getValueFactory();
				IValue delta = (IValue) eval.call("compareJars", "org::maracas::delta::JApiCmp",
						ImmutableMap.of("oldCP", vf.list(), "newCP", vf.list()),
						vf.sourceLocation(j1.toString()), vf.sourceLocation(j2.toString()), vf.string("v1"), vf.string("v2"));
				
				ISet changed = (ISet) eval.call("getChangedEntities", delta);
				
				StringBuilder sb = new StringBuilder();
				sb.append("{");
				sb.append("\"breakingChanges\":[");
				changed.forEach(e -> {
					ITuple tuple = (ITuple) e;
					String bc = tuple.get(0).toString();
					ISourceLocation decl = (ISourceLocation) tuple.get(1);
					
					sb.append("{\"decl\": " + decl.toString() + ", \"bc\": " + bc + "},");
				});
				sb.append("]}");

				return sb.toString();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}

		return "";
	}

	private Optional<Path> cloneAndBuild(String url, String ref, Path path) throws MavenInvocationException, GitAPIException, IOException {
		// FIXME
		String fullRef = "refs/heads/" + ref;
		if (!path.toFile().exists()) {
			logger.info("Cloning {} [{}]", url, ref);
			Git.cloneRepository()
				.setURI(url)
				.setBranchesToClone(Collections.singletonList(fullRef))
				.setBranch(fullRef)
				.setDirectory(path.toFile())
				.call();
		}
		
		Path target = path.resolve("target");
		if (!target.toFile().exists()) {
			Path pom = path.resolve("pom.xml");

			logger.info("Building {}", pom);
			Properties properties = new Properties();
			properties.setProperty("skipTests", "true");
			
		    InvocationRequest request = new DefaultInvocationRequest();
		    request.setPomFile(pom.toFile());
		    request.setGoals(Collections.singletonList("package"));
		    request.setProperties(properties);
		    request.setBatchMode(true);
		     
		    Invoker invoker = new DefaultInvoker();
		    invoker.setMavenHome(new File("/usr"));
		    invoker.execute(request);
		}
		
	    try (Stream<Path> walk = Files.walk(target, 1)) {
	        return walk.filter(f -> f.toString().endsWith(".jar")).findFirst();
	    }
	}

	@PostMapping("/delta")
	String delta(@RequestParam("jar1") MultipartFile jar1, @RequestParam("jar2") MultipartFile jar2, RedirectAttributes redirectAttrs) {
		Path j1 = Paths.get(UPLOAD_PATH).resolve("v1.jar");
		Path j2 = Paths.get(UPLOAD_PATH).resolve("v2.jar");
		
		try {
			jar1.transferTo(j1);
			jar2.transferTo(j2);
			
			Evaluator eval = createRascalEvaluator();
			IValueFactory vf = ValueFactoryFactory.getValueFactory();
			IValue delta = (IValue) eval.call("compareJars", "org::maracas::delta::JApiCmp",
					ImmutableMap.of("oldCP", vf.list(), "newCP", vf.list()),
					vf.sourceLocation(j1.toString()), vf.sourceLocation(j2.toString()), vf.string("v1"), vf.string("v2"));
			
			IString json = (IString) eval.call("toJSON", delta);
			
			Files.delete(j1);
			Files.delete(j2);
			
			return json.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "err";
		}
	}

	@PostMapping("/detections")
	String detections(@RequestParam("client") MultipartFile client, @RequestParam("jar1") MultipartFile jar1, @RequestParam("jar2") MultipartFile jar2, RedirectAttributes redirectAttrs) {
		Path c = Paths.get(UPLOAD_PATH).resolve("client.jar");
		Path j1 = Paths.get(UPLOAD_PATH).resolve("v1.jar");
		Path j2 = Paths.get(UPLOAD_PATH).resolve("v2.jar");
		Path deltaPath = Paths.get(UPLOAD_PATH).resolve("tmp.delta");

		try {
			client.transferTo(c);
			jar1.transferTo(j1);
			jar2.transferTo(j2);
			
			Evaluator eval = createRascalEvaluator();
			IValueFactory vf = ValueFactoryFactory.getValueFactory();
			Prelude prelude = new Prelude(vf, new PrintWriter(System.out), new PrintWriter(System.err));
			
			ISourceLocation v1Loc = vf.sourceLocation(j1.toString());
			ISourceLocation v2Loc = vf.sourceLocation(j2.toString());
			ISourceLocation deltaLoc = vf.sourceLocation(deltaPath.toString());
			IValue delta = (IValue) eval.call("compareJars", "org::maracas::delta::JApiCmp",
					ImmutableMap.of("oldCP", vf.list(), "newCP", vf.list()),
					v1Loc, v2Loc, vf.string("v1"), vf.string("v2"));
			prelude.writeBinaryValueFile(deltaLoc, delta, vf.bool(false));
			
			ISourceLocation clientLoc = vf.sourceLocation(c.toString());
			IValue detections = (ISet) eval.call("computeDetections",
				(IValue) clientLoc, (IValue) v1Loc, (IValue) v2Loc, (IValue) deltaLoc);
			
			IString json = (IString) eval.call("toJSON", detections);
			
			Files.delete(j1);
			Files.delete(j2);
			
			return json.toString(); 
		} catch (IOException e) {
			e.printStackTrace();
			return "err";
		}
	}

	@GetMapping("/clients/{g}/{a}/{v}")
	String clients(@PathVariable("g") String group, @PathVariable("a") String artifact, @PathVariable("v") String version) {
		try (
			Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "j4oen"));
			Session session = driver.session(AccessMode.READ)
		) {
			StatementResult result = session.run(
					"MATCH (c)-[:DEPENDS_ON]->(l) \n" + 
					"WHERE l.coordinates = {coord} " + 
					"RETURN c.coordinates",
					ImmutableMap.<String, Object>builder()
						.put("coord", String.format("%s:%s:%s", group, artifact, version))
						.build()
				);

				Gson gson = new Gson();
				StringBuilder builder = new StringBuilder();
				while (result.hasNext()) {
					Record record = result.next();
				    builder.append(gson.toJson(record.asMap()));
				}
				return builder.toString();
		}
	}

	private synchronized Evaluator createRascalEvaluator() {
		IValueFactory vf = ValueFactoryFactory.getValueFactory();
		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment module = new ModuleEnvironment("$maracas-rest$", heap);
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
		eval.doImport(new NullRascalMonitor(), "lang::json::IO");

		return eval;
	}
}
