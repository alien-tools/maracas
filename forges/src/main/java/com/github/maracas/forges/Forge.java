package com.github.maracas.forges;

import com.github.maracas.forges.github.BreakbotConfig;

import java.util.List;

public interface Forge {
  Repository fetchRepository(String owner, String name) throws ForgeException;

  Repository fetchRepository(String owner, String name, String branch) throws ForgeException;

  PullRequest fetchPullRequest(Repository repository, int number) throws ForgeException;

  default PullRequest fetchPullRequest(String owner, String name, int number) throws ForgeException {
    return fetchPullRequest(fetchRepository(owner, name), number);
  }

  Commit fetchCommit(Repository repository, String sha) throws ForgeException;

  default Commit fetchCommit(String owner, String name, String sha) throws ForgeException {
    return fetchCommit(fetchRepository(owner, name), sha);
  }

  List<Repository> fetchTopStarredClients(RepositoryModule module, int limit, int minStars);

  List<Repository> fetchAllClients(RepositoryModule module, int limit, int minStars);

  List<RepositoryModule> fetchModules(Repository repository);

  BreakbotConfig fetchBreakbotConfig(Repository repository);
}
