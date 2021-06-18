package org.swat.maracas.spoon.tests.compchanges;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.swat.maracas.spoon.Detection;
import org.swat.maracas.spoon.Detection.APIUse;

import japicmp.model.JApiCompatibilityChange;
import spoon.reflect.cu.SourcePosition;

public class DetectionAssertions {
	public static void assertDetection(Set<Detection> detections, String file, int line, JApiCompatibilityChange change, APIUse use) {
		Optional<Detection> find =
			detections.stream().filter(d -> {
				if (change != d.getChange())
					return false;
				if (use != d.getUse())
					return false;

				SourcePosition pos = d.getElement().getPosition();
				if (!file.equals(pos.getFile().getName().toString()))
					return false;
				if (line != pos.getLine())
					return false;
				
				return true;
			}).findAny();

		assertTrue(
			find.isPresent(),
			String.format("No detection found in %s:%d [%s] [%s]",
				file, line, change, use)
		);
	}
}
