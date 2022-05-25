package com.github.maracas.experiments.model;

import java.time.LocalDate;

/**
 * Represents a Pull Request (PR) in the repository.
 */
public class PullRequest {

	/**
	 * Main repository where the PR is being merged
	 */
	private final Repository repository;

	/**
	 * Base repository of the PR
	 */
	private final String baseRepository;

	/**
	 * Title of the PR
	 */
	private final String title;

	/**
	 * ID of the PR
	 */
	private final int id;

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
	enum State {
		OPEN, MERGED, CLOSED;
	}

	/**
	 * Crates a {@link PullRequest} instance. {@link LocalDate} fields are
	 * initialized to
	 *
	 * @param repository     Repository where the PR is created
	 * @param baseRepository Base repository of the PR
	 * @param title          Title of the PR
	 * @param id             ID of the PR
	 * @param state          {@link State} of the PR
	 * @param draft          Flag indicating if the PR is a draft
	 */
	public PullRequest(Repository repository, String baseRepository, String title,
		int id, State state, boolean draft) {
		this.repository = repository;
		this.baseRepository = baseRepository;
		this.title = title;
		this.id = id;
		this.state = state;
		this.draft = draft;
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
	 * Returns the PR {@link Repository}.
	 *
	 * @return the PR {@link Repository}
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
		return id;
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
