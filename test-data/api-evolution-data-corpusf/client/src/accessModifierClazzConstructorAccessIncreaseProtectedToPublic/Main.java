package accessModifierClazzConstructorAccessIncreaseProtectedToPublic;

import testing_lib.accessModifierClazzConstructorAccessIncreaseProtectedToPublic.AccessModifierClazzConstructorAccessIncreaseProtectedToPublic;

public class Main extends AccessModifierClazzConstructorAccessIncreaseProtectedToPublic {

	protected Main(int a, int b, int c, int d, int e) {
		super(a, b, c, d, e);
	}

	public static void main(String[] args) {
		int a = 5;
				
		new Main(a, a, a, a, a);
	}
	
}
