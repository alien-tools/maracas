package accessModifierClazzFieldAccessDecreasePublicToProtected1;

import testing_lib.accessModifierClazzFieldAccessDecreasePublicToProtected.AccessModifierClazzFieldAccessDecreasePublicToProtected;

public class Main extends AccessModifierClazzFieldAccessDecreasePublicToProtected {

	public static void main(String[] args) {
		
		Main constrExtended = new Main();
		
		Integer test = constrExtended.fieldPublicToProtected;
	}
	
}
