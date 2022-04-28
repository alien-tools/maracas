package accessModifierClazzConstructorAccessDecreasePublicToPrivate;

import testing_lib.accessModifierClazzConstructorAccessDecreasePublicToPrivate.AccessModifierClazzConstructorAccessDecreasePublicToPrivate;

public class Main extends AccessModifierClazzConstructorAccessDecreasePublicToPrivate {

	public Main(int a, int b) {
		super(a, b);
	}
	
	public static void main(String[] args) {
		
		int a = 5;
		
		new AccessModifierClazzConstructorAccessDecreasePublicToPrivate(a, a);
		
		new Main(a, a);
	}
	
}
