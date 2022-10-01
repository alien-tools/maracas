package com.github.maracas.forges.github;

import com.github.maracas.forges.Commit;
import com.github.maracas.forges.Forge;
import com.github.maracas.forges.ForgeException;
import com.github.maracas.forges.PullRequest;
import com.github.maracas.forges.Repository;
import org.kohsuke.github.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GitHubForge implements Forge {
	private final GitHub gh;

	public GitHubForge(GitHub gh) {
		this.gh = gh;
	}

	@Override
	public Repository fetchRepository(String owner, String name) {
		Objects.requireNonNull(owner);
		Objects.requireNonNull(name);
		String fullName = owner + "/" + name;

		try {
			GHRepository repo = gh.getRepository(fullName);
			return new Repository(
				repo.getOwnerName(),
				repo.getName(),
				repo.getHttpTransportUrl(),
				repo.getDefaultBranch()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch repository " + fullName, e);
		}
	}

	@Override
	public Repository fetchRepository(String owner, String name, String branch) {
		Objects.requireNonNull(owner);
		Objects.requireNonNull(name);
		Objects.requireNonNull(branch);
		String fullName = owner + "/" + name;

		try {
			GHRepository repo = gh.getRepository(fullName);
			GHBranch b = repo.getBranch(branch);

			return new Repository(
				repo.getOwnerName(),
				repo.getName(),
				repo.getHttpTransportUrl(),
				b.getName()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch repository %s on branch %s".formatted(fullName, branch), e);
		}
	}

	@Override
	public PullRequest fetchPullRequest(Repository repository, int number) {
		Objects.requireNonNull(repository);

		try {
			GHRepository repo = gh.getRepository(repository.fullName());
			GHPullRequest pr = repo.getPullRequest(number);
			GHCompare compare = repo.getCompare(pr.getBase().getCommit(), pr.getHead().getCommit());

			Commit base = new Commit(repository, pr.getBase().getSha());
			Commit head = new Commit(repository, pr.getHead().getSha());
			Commit mergeBase = new Commit(repository, compare.getMergeBaseCommit().getSHA1());
			List<GHPullRequestFileDetail> changedFiles = pr.listFiles().toList();

			return new PullRequest(
				repository,
				pr.getNumber(),
				base,
				head,
				mergeBase,
				pr.getBase().getRef(),
				pr.getHead().getRef(),
				changedFiles.stream().map(GHPullRequestFileDetail::getFilename).map(Path::of).toList()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch PR %d from repository %s".formatted(number, repository.fullName()), e);
		}
	}

	@Override
	public Commit fetchCommit(Repository repository, String sha) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(sha);

		try {
			GHCommit commit = gh.getRepository(repository.fullName()).getCommit(sha);

			return new Commit(
				repository,
				commit.getSHA1()
			);
		} catch (IOException e) {
			throw new ForgeException("Couldn't fetch commit %s from repository %s".formatted(sha, repository.fullName()), e);
		}
	}

	@Override
	public List<Repository> fetchTopClients(Repository repository, String packageId, int limit) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(packageId);
		if (limit < 1)
			throw new IllegalArgumentException("limit < 1");

		return
			new GitHubClientsFetcher(repository)
				.fetchClients(packageId)
				.stream()
				.sorted(Comparator.comparingInt(GitHubClientsFetcher.Client::stars).reversed())
				.limit(limit)
				.map(client -> fetchRepository(client.owner(), client.name()))
				.toList();
	}

	@Override
	public List<Repository> fetchStarredClients(Repository repository, String packageId, int stars) {
		Objects.requireNonNull(repository);
		Objects.requireNonNull(packageId);

		return
			new GitHubClientsFetcher(repository)
				.fetchClients(packageId)
				.stream()
				.sorted(Comparator.comparingInt(GitHubClientsFetcher.Client::stars).reversed())
				.filter(client -> client.stars() >= stars)
				.map(client -> fetchRepository(client.owner(), client.name()))
				.toList();
	}
}
