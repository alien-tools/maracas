package com.github.maracas;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record MaracasQuery(
	Path v1,
	Path v2,
	Path sources,
	Path output,
	List<Path> clients,
	List<Path> oldClasspath,
	List<Path> newClasspath
) {
	static public class Builder {
		private Path v1;
		private Path v2;
		private Path sources;
		private Path output;
		private List<Path> clients = new ArrayList<>();
		private List<Path> oldClasspath = new ArrayList<>();
		private List<Path> newClasspath = new ArrayList<>();

		public Builder v1(Path p) {
			this.v1 = p.toAbsolutePath();
			return this;
		}

		public Builder v2(Path p) {
			this.v2 = p.toAbsolutePath();
			return this;
		}

		public Builder sources(Path p) {
			this.sources = p.toAbsolutePath();
			return this;
		}

		public Builder output(Path p) {
			this.output = p.toAbsolutePath();
			return this;
		}

		public Builder client(Path p) {
			if (!this.clients.contains(p.toAbsolutePath()))
				this.clients.add(p.toAbsolutePath());
			return this;
		}

		public Builder clients(Collection<Path> ps) {
			ps.forEach(p -> client(p));
			return this;
		}

		public Builder oldClasspath(Path p) {
			if (!this.oldClasspath.contains(p.toAbsolutePath()))
				this.oldClasspath.add(p.toAbsolutePath());
			return this;
		}

		public Builder newClasspath(Path p) {
			if (!this.newClasspath.contains(p.toAbsolutePath()))
				this.newClasspath.add(p.toAbsolutePath());
			return this;
		}

		public MaracasQuery build() {
			validate();
			return new MaracasQuery(v1, v2, sources, output, clients,
				oldClasspath, newClasspath);
		}

		private void validate() {
			if (!isValidJar(v1))
				throw new IllegalArgumentException("v1 isn't a valid JAR: " + v1);
			if (!isValidJar(v2))
				throw new IllegalArgumentException("v2 isn't a valid JAR: " + v2);
			if (sources != null && !isValidDirectory(sources))
				throw new IllegalArgumentException("sources isn't a valid directory: " + sources);

			clients.forEach(c -> {
				if (!isValidDirectory(c))
					throw new IllegalArgumentException("client isn't a valid directory: " + c);
			});

			oldClasspath.forEach(cp -> {
				if (!isValidJar(cp))
					throw new IllegalArgumentException("oldClasspath isn't a valid JAR: " + cp);
			});

			newClasspath.forEach(cp -> {
				if (!isValidJar(cp))
					throw new IllegalArgumentException("newClasspath isn't a valid JAR: " + cp);
			});
		}

		private boolean isValidJar(Path p) {
			return
				p != null &&
				Files.exists(p) &&
				Files.isRegularFile(p) &&
				p.getFileName().toString().endsWith(".jar");
		}

		private boolean isValidDirectory(Path p) {
			return
				p != null &&
				Files.exists(p) &&
				Files.isDirectory(p);
		}
	}
}
