package accessModifierClazzNestedClazzAccessDecrease2;

import testing_lib.accessModifierClazzNestedClazzAccessDecrease.AccessModifierClazzNestedClazzAccessDecrease;

public class Main extends AccessModifierClazzNestedClazzAccessDecrease {

	public static void main(String[] args) {
		AccessModifierClazzNestedClazzAccessDecrease constr = new Main();
		
		constr.new ClazzPublicToPrivate();
	}
	
}
