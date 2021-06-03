package org.swat.maracas.rest;

import org.junit.jupiter.api.Test;

class GithubRestTests {
	@Test
	void testPR() {
		GithubController ctrl = new GithubController();
		
		ctrl.analyzePullRequest("hub4j", "github-api", 1142);
	}
}
