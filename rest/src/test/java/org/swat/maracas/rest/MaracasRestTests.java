package org.swat.maracas.rest;

import org.junit.jupiter.api.Test;

class MaracasRestTests {
	@Test
	void testPR() {
		MaracasController ctrl = new MaracasController();
		
		ctrl.delta("hub4j", "github-api", 1142);
	}
}
