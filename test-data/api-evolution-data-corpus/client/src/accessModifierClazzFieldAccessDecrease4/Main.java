package accessModifierClazzFieldAccessDecrease4;

import testing_lib.accessModifierClazzFieldAccessDecrease.AccessModifierClazzFieldAccessDecrease;

public class Main extends AccessModifierClazzFieldAccessDecrease {

	public static void main(String[] args) {
		
		Main constrExtended = new Main();
		
		Integer test = constrExtended.fieldProtectedToPrivate;

	}
	
}
