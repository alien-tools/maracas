package accessModifierClazzFieldAccessDecreaseProtectedToPrivate;

import testing_lib.accessModifierClazzFieldAccessDecreaseProtectedToPrivate.AccessModifierClazzFieldAccessDecreaseProtectedToPrivate;

public class Main extends AccessModifierClazzFieldAccessDecreaseProtectedToPrivate {

	public static void main(String[] args) {
		
		AccessModifierClazzFieldAccessDecreaseProtectedToPrivate constr = new AccessModifierClazzFieldAccessDecreaseProtectedToPrivate();
		
		Main constrExtended = new Main();
		
		Integer test = constrExtended.fieldProtectedToPrivate;
	}
	
}
