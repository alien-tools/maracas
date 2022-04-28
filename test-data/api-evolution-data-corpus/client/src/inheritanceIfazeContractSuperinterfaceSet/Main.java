package inheritanceIfazeContractSuperinterfaceSet;

import testing_lib.inheritanceIfazeContractSuperinterfaceSet.InheritanceIfazeContractSuperinterfaceSet;
import testing_lib.inheritanceIfazeContractSuperinterfaceSet.Interface2;

public class Main implements InheritanceIfazeContractSuperinterfaceSet {

	public static void main(String[] args) {
		Main constr = new Main();
		Interface2 ifaze = (Interface2) constr;
		ifaze.ifaze2method1();
	}

	@Override
	public void ifaze2method1() {
		
	}
	
	@Override
	public void ifaze1method1() {
		
	}

	
}
