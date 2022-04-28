package accessModifierClazzConstructorAccessDecrease7;

import testing_lib.accessModifierClazzConstructorAccessDecrease.AccessModifierClazzConstructorAccessDecrease;

public class Main extends AccessModifierClazzConstructorAccessDecrease {

	public Main(int a) {
		super(a);
	}
	
	public static void main(String[] args) {
		int a = 5;
		Main m = new Main(a);
	}
	
}
