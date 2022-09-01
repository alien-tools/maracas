package com.github.maracas;

import javax.tools.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class OnTheFlyMaracasCase {
	private static final String TMP_PATH = System.getProperty("java.io.tmpdir");

	private Path saveSource(String source, String className) throws IOException {
		Path sourcePath = Paths.get(TMP_PATH, "%s.java".formatted(className));
		Files.writeString(sourcePath, source);
		return sourcePath;
	}

	private Path compileSources(String className, Path... sources) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> cus = fileManager.getJavaFileObjects(sources);

		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, cus);
		task.call();

		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
			System.out.format("%s on line %d in %s [%s]: %s%n",
				diagnostic.getKind(),
				diagnostic.getLineNumber(),
				diagnostic.getSource().toUri(),
				diagnostic.getCode(),
				diagnostic.getMessage(null));
		}

		return sources[0].getParent().resolve("%s.class".formatted(className));
	}

	private void createJar(Path jar, Collection<Path> classFiles) throws IOException {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream target = new JarOutputStream(new FileOutputStream(jar.toFile()), manifest);
		for (Path classFile : classFiles)
			add(classFile, target);
		target.close();
	}

	private void add(Path source, JarOutputStream target) throws IOException {
		JarEntry entry = new JarEntry(source.getFileName().toString());
		entry.setTime(source.toFile().lastModified());
		target.putNextEntry(entry);
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source.toFile()))) {
			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		}
	}

	public static AnalysisResult maracasCase(String oldCls, String oldClsName, String newCls, String newClsName, String clientCode) throws IOException {
		OnTheFlyMaracasCase otf = new OnTheFlyMaracasCase();

		Path oldClassFile = otf.compileSources(oldClsName, otf.saveSource(oldCls, oldClsName));
		Path oldJar = Paths.get(TMP_PATH,"old.jar");
		otf.createJar(oldJar, List.of(oldClassFile));

		Path newClassFile = otf.compileSources(newClsName, otf.saveSource(newCls, newClsName));
		Path newJar = Paths.get(TMP_PATH, "new.jar");
		otf.createJar(newJar, List.of(newClassFile));

		Path clientPath = Paths.get(TMP_PATH).resolve("client");
		Path clientFile = clientPath.resolve("src/main/java/Client.java");
		Path pomFile = clientPath.resolve("pom.xml");
		clientFile.toFile().getParentFile().mkdirs();
		Files.writeString(clientFile, clientCode);
		Files.writeString(pomFile, "<project></project>");

		Library oldVersion = new Library(oldJar.toAbsolutePath());
		Library newVersion = new Library(newJar.toAbsolutePath());
		Client client = new Client(clientPath.toAbsolutePath(), oldVersion);
		AnalysisQuery query = AnalysisQuery.builder()
			.oldVersion(oldVersion)
			.newVersion(newVersion)
			.client(client)
			.build();

		return Maracas.analyze(query);
	}
}
