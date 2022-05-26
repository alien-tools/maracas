package dataTypeIfazeMethodParamMutation;

import testing_lib.dataTypeIfazeMethodParamMutation.DataTypeIfazeMethodParamMutation;

public class Main implements DataTypeIfazeMethodParamMutation{

	@Override
	public void method1(Integer param1) {
		
	}
	
	public static void main(String[] args) {
		DataTypeIfazeMethodParamMutation ifaze = new Main();
		ifaze.method1(5);
	}

}
