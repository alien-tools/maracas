package com.github.maracas;

import com.github.maracas.brokenuse.APIUse;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.delta.BreakingChange;

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

		assertThat(res.delta().getBreakingChanges(), hasSize(1));
		assertThat(res.allBrokenUses(), hasSize(1));

		BreakingChange bc = res.delta().getBreakingChanges().stream().findFirst().get();
		BrokenUse d = res.allBrokenUses().stream().findFirst().get();

		assertThat(bc.getChange(), equalTo(JApiCompatibilityChange.METHOD_REMOVED));
		assertThat(d.use(), equalTo(APIUse.METHOD_INVOCATION));
	}
}
