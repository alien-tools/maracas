package accessModifierClazzConstructorAccessIncrease;

import testing_lib.accessModifierClazzConstructorAccessIncrease.AccessModifierClazzConstructorAccessIncrease;

public class Main extends AccessModifierClazzConstructorAccessIncrease {

	protected Main(int a, int b, int c, int d, int e) {
		super(a, b, c, d, e);
	}

	public static void main(String[] args) {
		int a = 5;
		new Main(a, a, a, a, a);
	}
	
}
