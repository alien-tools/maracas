package dataTypeIfazeMethodReturnTypeNarrowing;

import testing_lib.dataTypeIfazeMethodReturnTypeNarrowing.DataTypeIfazeMethodReturnTypeNarrowing;

public class Main implements DataTypeIfazeMethodReturnTypeNarrowing{

	@Override
	public double method1() {
		double result = 2.5;
		return result;
	}

	public static void main(String[] args) {
		DataTypeIfazeMethodReturnTypeNarrowing ifaze = new Main();
		double test = ifaze.method1();
	}
}
