package accessModifierClazzConstructorAccessDecrease1;

import testing_lib.accessModifierClazzConstructorAccessDecrease.AccessModifierClazzConstructorAccessDecrease;

public class Main extends AccessModifierClazzConstructorAccessDecrease {

	protected Main(int a, int b, int c, int d) {
		super(a, b, c, d);
	}
	
	public static void main(String[] args) {
		
		int a = 5;
		
		new Main(a, a, a, a);
	}
	
}
