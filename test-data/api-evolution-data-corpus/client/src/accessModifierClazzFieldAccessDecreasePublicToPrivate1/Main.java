package accessModifierClazzFieldAccessDecreasePublicToPrivate1;

import testing_lib.accessModifierClazzFieldAccessDecreasePublicToPrivate.AccessModifierClazzFieldAccessDecreasePublicToPrivate;

public class Main extends AccessModifierClazzFieldAccessDecreasePublicToPrivate {

	public static void main(String[] args) {
		
		Main constrExtended = new Main();
		
		Integer test = constrExtended.fieldPublicToPrivate;
	}
	
}
