package accessModifierClazzConstructorAccessDecrease8;

import testing_lib.accessModifierClazzConstructorAccessDecrease.AccessModifierClazzConstructorAccessDecrease;

public class Main extends AccessModifierClazzConstructorAccessDecrease {

	public Main(int a, int b) {
		super(a, b);
	}
	
	public static void main(String[] args) {
		int a = 5;
		Main m = new Main(a, a);
	}
	
}
