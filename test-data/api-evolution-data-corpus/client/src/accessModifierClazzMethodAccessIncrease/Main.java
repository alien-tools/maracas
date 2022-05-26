package accessModifierClazzMethodAccessIncrease;

import testing_lib.accessModifierClazzMethodAccessIncrease.AccessModifierClazzMethodAccessIncrease;

public class Main extends AccessModifierClazzMethodAccessIncrease {

	public static void main(String[] args) {
		Main constrExtended = new Main();
		constrExtended.methodProtectedToPublic();
	}
	
}
