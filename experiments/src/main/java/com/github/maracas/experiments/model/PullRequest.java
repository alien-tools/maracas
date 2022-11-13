package com.github.maracas.experiments.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.maracas.experiments.utils.Util;

/**
 * Represents a Pull Request (PR) in the repository.
 */
public class PullRequest {
	/**
	 * Title of the PR
	 */
	private final String title;

	/**
	 * Number of the PR
	 */
	private final int number;

	/**
	 * Repository where the PR is being merged
	 */
	private final Repository repository;

	/**
	 * Base repository of the PR
	 */
	private final String baseRepository;

	/**
	 * Base reference of the PR
	 */
	private final String baseRef;

	/**
	 * Base reference prefix of the PR
	 */
	private final String baseRefPrefix;

	/**
	 * Head repository of the PR
	 */
	private final String headRepository;

	/**
	 * Head reference of the PR
	 */
	private final String headRef;

	/**
	 * Head reference prefix of the PR
	 */
	private final String headRefPrefix;

	/**
	 * State of the PR
	 */
	private final State state;

	/**
	 * Flag indicating if the PR is a draft
	 */
	private final boolean draft;

	/**
	 * {@link LocalDate} with the PR creation date
	 */
	private LocalDate createdAt;

	/**
	 * {@link LocalDate} with the PR publication date
	 */
	private LocalDate publishedAt;

	/**
	 * {@link LocalDate} with the PR merge date
	 */
	private LocalDate mergedAt;

	/**
	 * {@link LocalDate} with the PR closing date
	 */
	private LocalDate closedAt;

	/**
	 * List with the relative paths of the modified files
	 */
	private List<String> files;

	/**
	 * Constants representing the state of a PR
	 */
	public enum State {
		OPEN, MERGED, CLOSED;
	}

	/**
	 * Creates a {@link PullRequest} instance.
	 *
	 * @param title           Title of the PR
	 * @param number          Number of the PR
	 * @param repository      Repository where the PR is being merged
	 * @param baseRepository  Base repository of the PR
	 * @param baseRef         Base reference of the PR
	 * @param baseRefPrefix   Base reference prefix of the PR
	 * @param headRepository  Head repository of the PR
	 * @param headRef         Head reference of the PR
	 * @param headRefPrefix   Head reference prefix of the PR
	 * @param state           {@link State} of the PR
	 * @param draft           Flag indicating if the PR is a draft
	 * @param createdAt       String representing the PR creation date (e.g. "2022-01-01T20:00:00Z")
	 * @param publishedAt     String representing the PR publication date (e.g. "2022-01-01T20:00:00Z")
	 * @param mergedAt        String representing the PR merge date (e.g. "2022-01-31T20:00:00Z")
	 * @param closedAt        String representing the PR closing date (e.g. "2022-01-31T20:00:00Z")
	 */
	public PullRequest(String title, int number, Repository repository, String baseRepository,
		String baseRef, String baseRefPrefix, String headRepository, String headRef, String headRefPrefix,
		State state, boolean draft, String createdAt, String publishedAt, String mergedAt, String closedAt) {
		this.title = title;
		this.number = number;
		this.repository = repository;
		this.baseRepository = baseRepository;
		this.baseRef = baseRef;
		this.baseRefPrefix = baseRefPrefix;
		this.headRepository = headRepository;
		this.headRef = headRef;
		this.headRefPrefix = headRefPrefix;
		this.state = state;
		this.draft = draft;
		setCreatedAt(createdAt);
		setPublishedAt(publishedAt);
		setMergedAt(mergedAt);
		setClosedAt(closedAt);
		this.files = new ArrayList<String>();
	}

	/**
	 * Returns the PR title.
	 *
	 * @return the PR title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the PR number.
	 *
	 * @return PR number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Returns the repository where the PR is being merged.
	 *
	 * @return Repository where the PR is being merged
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * Returns the PR base repository.
	 *
	 * @return the PR base repository
	 */
	public String getBaseRepository() {
		return baseRepository;
	}

	/**
	 * Returns the PR base reference.
	 *
	 * @return the PR base reference
	 */
	public String getBaseRef() {
		return baseRef;
	}

	/**
	 * Returns the PR base reference prefix.
	 *
	 * @return the PR base reference prefix
	 */
	public String getBaseRefPrefix() {
		return baseRefPrefix;
	}

	/**
	 * Returns the PR head repository.
	 *
	 * @return the PR head repository
	 */
	public String getHeadRepository() {
		return headRepository;
	}

	/**
	 * Returns the PR head reference.
	 *
	 * @return the PR head reference
	 */
	public String getHeadRef() {
		return headRef;
	}

	/**
	 * Returns the PR head reference prefix.
	 *
	 * @return the PR head reference prefix
	 */
	public String getHeadRefPrefix() {
		return headRefPrefix;
	}

	/**
	 * Returns the PR {@link State}.
	 *
	 * @return the PR {@link State}
	 */
	public State getState() {
		return state;
	}

	/**
	 * Indicates if the PR is a draft.
	 * @return {@code true} if the PR is a draft, {@code false} otherwise
	 */
	public boolean isDraft() {
		return draft;
	}

	/**
	 * Returns the PR creation {@link LocalDate}.
	 *
	 * @return the PR creation {@link LocalDate}
	 */
	public LocalDate getCreatedAt() {
		return createdAt;
	}

	/**
	 * Sets the PR creation {@link LocalDate}.
	 *
	 * @param createdAt PR creation {@link LocalDate}
	 */
	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Sets the PR creation {@link LocalDate}.
	 *
	 * @param createdAt String representing the PR creation date
	 */
	public void setCreatedAt(String createdAt) {
		if (createdAt != null && !createdAt.isEmpty() && !Util.isNullValue(createdAt))
			this.createdAt = Util.stringToLocalDate(createdAt);
	}

	/**
	 * Returns the PR publication {@link LocalDate}.
	 *
	 * @return the PR publication {@link LocalDate}
	 */
	public LocalDate getPublishedAt() {
		return publishedAt;
	}

	/**
	 * Sets the PR publication {@link LocalDate}.
	 *
	 * @param publishedAt PR publication {@link LocalDate}
	 */
	public void setPublishedAt(LocalDate publishedAt) {
		this.publishedAt = publishedAt;
	}

	/**
	 * Sets the PR publication {@link LocalDate}.
	 *
	 * @param publishedAt String representing the PR publication date
	 */
	public void setPublishedAt(String publishedAt) {
		if (publishedAt != null && !publishedAt.isEmpty() && !Util.isNullValue(publishedAt))
			this.publishedAt = Util.stringToLocalDate(publishedAt);
	}

	/**
	 * Returns the PR merge {@link LocalDate}.
	 *
	 * @return the PR merge {@link LocalDate}
	 */
	public LocalDate getMergedAt() {
		return mergedAt;
	}

	/**
	 * Sets the PR merge {@link LocalDate}.
	 *
	 * @param mergedAt PR merge {@link LocalDate}
	 */
	public void setMergedAt(LocalDate mergedAt) {
		this.mergedAt = mergedAt;
	}

	/**
	 * Sets the PR merge {@link LocalDate}.
	 *
	 * @param mergedAt String representing the PR merge date
	 */
	public void setMergedAt(String mergedAt) {
		if (mergedAt != null && !mergedAt.isEmpty() && !Util.isNullValue(mergedAt))
			this.mergedAt = Util.stringToLocalDate(mergedAt);
	}

	/**
	 * Returns the PR closing {@link LocalDate}.
	 *
	 * @return the PR closing {@link LocalDate}
	 */
	public LocalDate getClosedAt() {
		return closedAt;
	}

	/**
	 * Sets the PR closing {@link LocalDate}.
	 *
	 * @param closedAt PR closing {@link LocalDate}
	 */
	public void setClosedAt(LocalDate closedAt) {
		this.closedAt = closedAt;
	}

	/**
	 * Sets the PR closing {@link LocalDate}.
	 *
	 * @param closedAt String representing the PR closing date
	 */
	public void setClosedAt(String closedAt) {
		if (closedAt != null && !closedAt.isEmpty() && !Util.isNullValue(closedAt))
			this.closedAt = Util.stringToLocalDate(closedAt);
	}

	/**
	 * Returns the list of paths of the modified files.
	 *
	 * @return List of paths of the modified files
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Sets the list of paths of the modified files.
	 *
	 * @param files List of paths of the modified files
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

	/**
	 * Adds a new file relative path to the files list.
	 *
	 * @param file Relative path to the new file.
	 */
	public void addFile(String file) {
		if (file != null && !file.isEmpty())
			files.add(file);
	}
}
