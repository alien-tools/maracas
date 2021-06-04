package org.swat.maracas.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import io.usethesource.vallang.IValue;

@RestController
public class MaracasController {
	private MaracasHelper maracas = MaracasHelper.getInstance();
	private static final String UPLOAD_PATH = "/home/dig/tmp";

	@PostMapping("/delta")
	String delta(@RequestParam("jar1") MultipartFile jar1, @RequestParam("jar2") MultipartFile jar2, RedirectAttributes redirectAttrs) {
		Path j1 = Paths.get(UPLOAD_PATH).resolve("v1.jar");
		Path j2 = Paths.get(UPLOAD_PATH).resolve("v2.jar");
		
		try {
			jar1.transferTo(j1);
			jar2.transferTo(j2);
			
			//IValue delta = maracas.computeDelta(j1, j2);
			
			Files.delete(j1);
			Files.delete(j2);
			
			return "";
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
}
