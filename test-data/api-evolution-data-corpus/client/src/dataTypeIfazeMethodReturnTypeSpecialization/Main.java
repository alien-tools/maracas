package dataTypeIfazeMethodReturnTypeSpecialization;

import testing_lib.dataTypeIfazeMethodReturnTypeSpecialization.DataTypeIfazeMethodReturnTypeSpecialization;

public class Main implements DataTypeIfazeMethodReturnTypeSpecialization {

	@Override
	public Number method1() {
		return null;
	}
	
	public static void main(String[] args) {
		DataTypeIfazeMethodReturnTypeSpecialization ifaze = new Main();
		Number test = ifaze.method1();
	}
}
