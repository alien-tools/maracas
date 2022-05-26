package dataTypeIfazeMethodParamBoxing;

import testing_lib.dataTypeIfazeMethodParamBoxing.DataTypeIfazeMethodParamBoxing;

public class Main implements DataTypeIfazeMethodParamBoxing {

	@Override
	public void method1(int param1) {

	}
	
	public static void main(String[] args) {
		DataTypeIfazeMethodParamBoxing ifaze = new Main();
		ifaze.method1(5);
	}

}
