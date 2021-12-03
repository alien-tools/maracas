package com.github.maracas;

import com.github.maracas.delta.BrokenDeclaration;
import com.github.maracas.detection.APIUse;
import com.github.maracas.detection.Detection;
import japicmp.model.JApiCompatibilityChange;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static com.github.maracas.OnTheFlyMaracasCase.maracasCase;

class OnTheFlyTest {
	@Test
	void removed_Method_Breaks_Invocation() throws Exception {
		AnalysisResult res = maracasCase(
			"class Foo { void foo() {} }", "Foo",
			"class Foo {}", "Foo",
			"""
				class Client {
					void bar() {
						Foo f = new Foo();
						f.foo();
					}
				}
			""");

		assertThat(res.delta().getBrokenDeclarations(), hasSize(1));
		assertThat(res.allDetections(), hasSize(1));

		BrokenDeclaration decl = res.delta().getBrokenDeclarations().stream().findFirst().get();
		Detection d = res.allDetections().stream().findFirst().get();

		assertThat(decl.getChange(), equalTo(JApiCompatibilityChange.METHOD_REMOVED));
		assertThat(d.use(), equalTo(APIUse.METHOD_INVOCATION));
	}
}
