package accessModifierClazzConstructorAccessDecreaseProtectedToPrivate;

import testing_lib.accessModifierClazzConstructorAccessDecreaseProtectedToPrivate.AccessModifierClazzConstructorAccessDecreaseProtectedToPrivate;

public class Main extends AccessModifierClazzConstructorAccessDecreaseProtectedToPrivate {

	protected Main(int a, int b, int c, int d) {
		super(a, b, c, d);
	}
	
	public static void main(String[] args) {
		
		int a = 5;
		
		new Main(a, a, a, a);
	}
	
}
