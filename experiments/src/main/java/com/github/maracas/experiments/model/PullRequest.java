package com.github.maracas.experiments.model;

import java.time.LocalDate;

import com.github.maracas.experiments.utils.Util;

/**
 * Represents a Pull Request (PR) in the repository.
 */
public class PullRequest {
	/**
	 * Base repository of the PR
	 */
	private final String baseRepository;

	/**
	 * Title of the PR
	 */
	private final String title;

	/**
	 * Number of the PR
	 */
	private final int number;

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
	 * Constants representing the state of a PR
	 */
	public enum State {
		OPEN, MERGED, CLOSED;
	}

	/**
	 * Crates a {@link PullRequest} instance. {@link LocalDate} fields are
	 * initialized to
	 *
	 * @param number         Number of the PR
	 * @param title          Title of the PR
	 * @param baseRepository Base repository of the PR represented by the owner/repo
	 *                       string (e.g. "google/guava")
	 * @param state          {@link State} of the PR
	 * @param draft          Flag indicating if the PR is a draft
	 * @param createdAt      String representing the PR creation date (e.g. "2022-01-01T20:00:00Z")
	 * @param publishedAt    String representing the PR publication date (e.g. "2022-01-01T20:00:00Z")
	 * @param mergedAt       String representing the PR merge date (e.g. "2022-01-31T20:00:00Z")
	 * @param closedAt       String representing the PR closing date (e.g. "2022-01-31T20:00:00Z")
	 */
	public PullRequest(String title, int number, String baseRepository, State state,
		boolean draft, String createdAt, String publishedAt, String mergedAt, String closedAt) {
		this.title = title;
		this.number = number;
		this.baseRepository = baseRepository;
		this.state = state;
		this.draft = draft;
		setCreatedAt(createdAt);
		setPublishedAt(publishedAt);
		setMergedAt(mergedAt);
		setClosedAt(closedAt);
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
		if (!createdAt.isEmpty())
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
		if (!publishedAt.isEmpty())
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
		if (!mergedAt.isEmpty())
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
		if (!closedAt.isEmpty())
			this.closedAt = Util.stringToLocalDate(closedAt);
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
	 * Returns the PR title.
	 *
	 * @return the PR title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the PR ID.
	 *
	 * @return the PR ID
	 */
	public int getId() {
		return number;
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
	 * @return {@code true} if the PR is a drftf, {@code false} otherwise
	 */
	public boolean isDraft() {
		return draft;
	}

}
