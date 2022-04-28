package accessModifierClazzFieldAccessDecreaseProtectedToNon;

import testing_lib.accessModifierClazzFieldAccessDecreaseProtectedToNon.AccessModifierClazzFieldAccessDecreaseProtectedToNon;

public class Main extends AccessModifierClazzFieldAccessDecreaseProtectedToNon {

	public static void main(String[] args) {
		
		AccessModifierClazzFieldAccessDecreaseProtectedToNon constr = new AccessModifierClazzFieldAccessDecreaseProtectedToNon();
		
		Main constrExtended = new Main();
		
		Integer test = constrExtended.fieldProtectedToNon;
	}
	
}
