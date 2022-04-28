package modifierMethodAbstractToNonAbstract;

import testing_lib.modifierMethodAbstractToNonAbstract.ModifierMethodAbstractToNonAbstract;

public class Main extends ModifierMethodAbstractToNonAbstract {

	public static void main(String[] args) {
		Main constr = new Main();
		constr.method1();
	}

	@Override
	public void method1() {
		
	}
	
}
