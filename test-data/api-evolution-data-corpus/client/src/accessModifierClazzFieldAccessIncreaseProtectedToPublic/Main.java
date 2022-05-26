package accessModifierClazzFieldAccessIncreaseProtectedToPublic;

import testing_lib.accessModifierClazzFieldAccessIncreaseProtectedToPublic.AccessModifierClazzFieldAccessIncreaseProtectedToPublic;

public class Main extends AccessModifierClazzFieldAccessIncreaseProtectedToPublic {

	public static void main(String[] args) {
		
		Main constrExtended = new Main();
		
		Integer i = constrExtended.fieldProtectedToPublic;
	}
	
}
