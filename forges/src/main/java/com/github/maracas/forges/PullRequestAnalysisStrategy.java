package com.github.maracas.forges;

import com.github.maracas.forges.build.BuildConfig;

import java.nio.file.Path;
import java.util.List;

public interface PullRequestAnalysisStrategy {
	BuildConfig makeLibraryConfig(Commit c, Path module);
	BuildConfig makeClientConfig(Commit c);
	List<Commit> fetchClientsFor(Package pkg);
}
