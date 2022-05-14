package accessModifierClazzFieldAccessIncrease;

import testing_lib.accessModifierClazzFieldAccessIncrease.AccessModifierClazzFieldAccessIncrease;

public class Main extends AccessModifierClazzFieldAccessIncrease {

	public static void main(String[] args) {
		Main constrExtended = new Main();
		
		//System.out.println(constrExtended.fieldProtectedToPublic);
		int i = constrExtended.fieldProtectedToPublic;
	}
	
}
