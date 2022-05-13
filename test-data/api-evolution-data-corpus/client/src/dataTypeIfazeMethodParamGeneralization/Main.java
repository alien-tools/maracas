package dataTypeIfazeMethodParamGeneralization;

import testing_lib.dataTypeIfazeMethodParamGeneralization.DataTypeIfazeMethodParamGeneralization;

public class Main implements DataTypeIfazeMethodParamGeneralization{
	
	@Override
	public void method1(Integer param1) {
		
	}

	public static void main(String[] args) {
		DataTypeIfazeMethodParamGeneralization ifaze = new Main();
		ifaze.method1(5);
	}

}
