package com.github.maracas.forges;

public interface Forge {
  Repository fetchRepository(String owner, String name) throws ForgeException;
  PullRequest fetchPullRequest(Repository repository, int number) throws ForgeException;
}
