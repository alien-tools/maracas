package accessModifierClazzMethodAccessDecrease5;

import testing_lib.accessModifierClazzMethodAccessDecrease.AccessModifierClazzMethodAccessDecrease;

public class Main extends AccessModifierClazzMethodAccessDecrease {
	public void foo() {
		methodProtectedToNon();
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.foo();
	}
	
}
