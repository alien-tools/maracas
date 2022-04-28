package accessModifierClazzFieldAccessDecreasePublicToNon1;

import testing_lib.accessModifierClazzFieldAccessDecreasePublicToNon.AccessModifierClazzFieldAccessDecreasePublicToNon;

public class Main extends AccessModifierClazzFieldAccessDecreasePublicToNon {

	public static void main(String[] args) {
		
		Main constrExtended = new Main();
		
		Integer test = constrExtended.fieldPublicToNon;
	}
	
}
