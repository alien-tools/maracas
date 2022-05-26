package testing_lib.inheritanceIfazeDefaultMethodOverrideAdd;

import testing_lib.inheritanceIfazeDefaultMethodOverrideAdd.Interface1;

public interface InheritanceIfazeDefaultMethodOverrideAdd extends Interface1 {

	@Override
	public default void method1() {
		//System.out.println("InheritancIfazeDefaultMethodOverrideAdd default method1");
	}
	
}
