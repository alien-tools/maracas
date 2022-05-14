package accessModifierClazzMethodAccessIncreaseProtectedToPublic1;

import testing_lib.accessModifierClazzMethodAccessIncreaseProtectedToPublic.AccessModifierClazzMethodAccessIncreaseProtectedToPublic;

public class Main extends AccessModifierClazzMethodAccessIncreaseProtectedToPublic {

	public static void main(String[] args) {
		Main constrExtended = new Main();
		constrExtended.methodProtectedToPublic();
	}
	
}
