package modifierMethodNonFinalToFinal;

import testing_lib.modifierMethodNonFinalToFinal.ModifierMethodNonFinalToFinal;

public class Main extends ModifierMethodNonFinalToFinal {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.method1();
	}
	
	@Override
	public void method1() {
		super.method1();
	}
	
	
}
