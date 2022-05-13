package accessModifierClazzConstructorAccessDecrease;

import testing_lib.accessModifierClazzConstructorAccessDecrease.AccessModifierClazzConstructorAccessDecrease;

public class Main extends AccessModifierClazzConstructorAccessDecrease {

	protected Main(int a, int b, int c) {
		super(a, b, c);
	}
	
	public static void main(String[] args) {
		
		int a = 5;
		
		new Main(a, a, a);
		
	}
	
}
