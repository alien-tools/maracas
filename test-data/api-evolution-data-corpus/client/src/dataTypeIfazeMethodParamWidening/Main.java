package dataTypeIfazeMethodParamWidening;

import testing_lib.dataTypeIfazeMethodParamWidening.DataTypeIfazeMethodParamWidening;

public class Main implements DataTypeIfazeMethodParamWidening{

	@Override
	public void method1(int param1) {
		
	}

	public static void main(String[] args) {
		DataTypeIfazeMethodParamWidening ifaze = new Main();
		ifaze.method1(5);
	}
}
