package accessModifierClazzConstructorAccessDecreasePublicToNon;

import testing_lib.accessModifierClazzConstructorAccessDecreasePublicToNon.AccessModifierClazzConstructorAccessDecreasePublicToNon;

public class Main extends AccessModifierClazzConstructorAccessDecreasePublicToNon {

	public Main(int a) {
		super(a);
	}
	
	public static void main(String[] args) {
		
		int a = 5;
		
		new AccessModifierClazzConstructorAccessDecreasePublicToNon(a);
		
		new Main(a);
	}
	
}
