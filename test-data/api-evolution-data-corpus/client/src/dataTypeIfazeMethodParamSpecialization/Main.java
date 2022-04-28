package dataTypeIfazeMethodParamSpecialization;

import testing_lib.dataTypeIfazeMethodParamSpecialization.DataTypeIfazeMethodParamSpecialization;

public class Main implements DataTypeIfazeMethodParamSpecialization{

	@Override
	public void method1(Number param1) {
		
	}

	public static void main(String[] args) {
		DataTypeIfazeMethodParamSpecialization ifaze = new Main();
		ifaze.method1(5);
	}
}
