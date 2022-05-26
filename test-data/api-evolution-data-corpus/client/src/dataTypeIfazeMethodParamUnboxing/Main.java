package dataTypeIfazeMethodParamUnboxing;

import testing_lib.dataTypeIfazeMethodParamUnboxing.DataTypeIfazeMethodParamUnboxing;

public class Main implements DataTypeIfazeMethodParamUnboxing{

	@Override
	public void method1(Integer param1) {
		
	}

	public static void main(String[] args) {
		DataTypeIfazeMethodParamUnboxing ifaze = new Main();
		ifaze.method1(5);
	}
}
