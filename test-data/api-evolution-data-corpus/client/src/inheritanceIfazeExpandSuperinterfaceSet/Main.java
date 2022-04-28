package inheritanceIfazeExpandSuperinterfaceSet;

import testing_lib.inheritanceIfazeExpandSuperinterfaceSet.InheritanceIfazeExpandSuperinterfaceSet;
import testing_lib.inheritanceIfazeExpandSuperinterfaceSet.Interface1;

public class Main implements InheritanceIfazeExpandSuperinterfaceSet {

	@Override
	public void ifaze1method1() {
		
	}
	
	public static void main(String[] args) {
		Main constr = new Main();
		Interface1 ifaze = constr;
		ifaze.ifaze1method1();
	}

}
