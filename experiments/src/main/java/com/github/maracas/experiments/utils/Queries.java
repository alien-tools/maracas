package com.github.maracas.experiments.utils;

/**
 * Helper class with the GitHub GraphQL queries used in the project.
 */
public final class Queries {

	/**
	 * Constructor of the class. This class must not be initialized. It only
	 * offers a set of constants.
	 */
	private Queries() {}

	/**
	 * Query to gather the 100 most popular Java libraries from GitHub. A
	 * repository is considered to be a popular library if:
	 * - it uses Java as one of its programming languages;
	 * - it has a minimum and maximum of stars (provided by the user);
	 * - it has been pushed after a given date (provided by the user);
	 * - it is not archived;
	 * - it is not a fork, and;
	 * - it is nor a mirror.
	 *
	 * The query returns the following fields from the GitHub schema: nameWithOwner
	 * (e.g. "google/guava"), stargazerCount (number of stars), isDisabled,
	 * isEmpty, isLocked, pushedAt (last push date), pom with oid field, and
	 * gradle with oid field. Additional checks can be performed over these
	 * fields.
	 *
	 * When using this query you should format the query with the following
	 * information in the given order:
	 * - Integer with minimum number of stars (e.g. 1)
	 * - Integer with maximum number of stars (e.g. 100)
	 * - String with last pushed date (e.g. "2022-01-01")
	 * - Cursor query (can be an empty string)
	 *
	 * Removed: sort:stars-desc
	 */
	public final static String GRAPHQL_LIBRARIES_QUERY = """
		query {
		  search(
		    type: REPOSITORY,
		    query: "java in:language stars:>%d pushed:>%s archived:false fork:false mirror:false",
		    first: 100
		    %s
		  ) {
		    repositoryCount

		    edges {
		      cursor
		      node {
		        ... on Repository {
		          nameWithOwner
		          stargazerCount
		          isDisabled
		          isEmpty
		          isLocked
		          createdAt
		          pushedAt
		          sshUrl
		          url

		          pom: object(expression: "HEAD:pom.xml") {
		            oid
		          }

		          gradle: object(expression: "HEAD:build.gradle") {
		            oid
		          }

		          pullRequests(states: [MERGED], last: 1) {
		            mergedPRs: totalCount

		            edges {
		              node {
		                mergedAt
		              }
		            }
		          }
		        }
		      }
		    }

		    pageInfo {
		      hasNextPage
		      endCursor
		    }
		  }
		}""";

	/**
	 * Query to validate if a client is a relevant client. A client is considered
	 * to be relevant if:
	 * - it uses Java as one of its programming languages;
	 * - it has a minimum and maximum of stars (provided by the user);
	 * - it has been pushed after a given date (provided by the user);
	 * - it is not archived;
	 * - it is not a fork, and;
	 * - it is nor a mirror.
	 *
	 * The query returns 0 or 1 if the repository complies with the previous
	 * requirements (c.f. repositoryCount). It also returns the following fields
	 * from the GitHub schema: stargazerCount (number of stars), isDisabled,
	 * isEmpty, isLocked, pushedAt (last push date), pom with oid field, and
	 * gradle with oid field. Additional checks can be performed over these
	 * fields.
	 *
	 * When using this query you should format the query with the following
	 * information in the given order:
	 * - String with the GitHub owner of the repository (e.g. "google")
	 * - String with the GitHub repository name (e.g. "guava")
	 * - Integer with minimum number of stars (e.g. 1)
	 * - Integer with maximum number of stars (e.g. 100)
	 * - String with last pushed date (e.g. "2022-01-01")
	 */
	public final static String GRAPHQL_CLIENT_QUERY = """
		{
		  repository(owner: "%s", name: "%s") {
		    stargazerCount
		    isArchived
		    isDisabled
		    isEmpty
		    isFork
		    isLocked
		    isMirror
		    createdAt
		    pushedAt
		    sshUrl
		    url

		    languages(first: 100) {
		      edges {
		        node {
		          name
		        }
		      }
		    }

		    pom: object(expression: "HEAD:pom.xml") {
		      oid
		    }

		    gradle: object(expression: "HEAD:build.gradle") {
		      oid
		    }
		  }
		}
		""";


	public final static String GRAPHQL_PRS_QUERY = """
		query {
		  search(
		    type: ISSUE
		    query: "repo:%s/%s is:pr created:>%s is:open",
		    first: 100
		    %s
		  ) {
		    edges {
		      node {
		        ... on PullRequest {

		          baseRef {
                    name
                    prefix
                  }

                  baseRepository {
                    nameWithOwner
                  }

                  headRef {
                    name
                    prefix
                  }

                  headRepository {
                    nameWithOwner
                  }

		          closedAt
		          createdAt
		          id
		          isDraft
		          mergedAt
		          number
		          publishedAt
		          state
		          title

		          files(first: 100) {
                    edges {
                      node {
                        path
                      }
                    }
                  }
		        }
		      }
		    }

		    pageInfo {
		      endCursor
		      hasNextPage
		    }
		  }
		}""";

	/**
	 * TODO: If the pull requests modifies more than 1000 files, we will only
	 * get 1000. We need to find a way to slice this query.
	 */
//	public final static String GRAPHQL_PRS_FILES_QUERY = """
//		query {
//		  search(
//		    type: REPOSITORY,
//		    query: "repo:%s/%s",
//		    first: 1
//		  ) {
//		    edges {
//		      node {
//		        ... on Repository {
//		          pr: pullRequest(number: %d) {
//
//		            files(first: 100 %s) {
//		              totalCount
//		              edges {
//		                node {
//		                  path
//		                }
//		                cursor
//		              }
//
//		              pageInfo {
//		                endCursor
//		                hasNextPage
//		              }
//		            }
//		          }
//		        }
//		      }
//		    }
//		  }
//		}""";

}