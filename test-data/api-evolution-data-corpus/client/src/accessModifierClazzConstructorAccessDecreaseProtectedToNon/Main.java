package accessModifierClazzConstructorAccessDecreaseProtectedToNon;

import testing_lib.accessModifierClazzConstructorAccessDecreaseProtectedToNon.AccessModifierClazzConstructorAccessDecreaseProtectedToNon;

public class Main extends AccessModifierClazzConstructorAccessDecreaseProtectedToNon {

	protected Main(int a, int b, int c) {
		super(a, b, c);
	}
	
	public static void main(String[] args) {
		
		int a = 5;
		
		new Main(a, a, a);
		
	}
	
}
