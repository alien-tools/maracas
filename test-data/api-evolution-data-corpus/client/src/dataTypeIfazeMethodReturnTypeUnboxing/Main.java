package dataTypeIfazeMethodReturnTypeUnboxing;

import testing_lib.dataTypeIfazeMethodReturnTypeUnboxing.DataTypeIfazeMethodReturnTypeUnboxing;

public class Main implements DataTypeIfazeMethodReturnTypeUnboxing{

	@Override
	public Integer method1() {
		return new Integer(5);
	}
	
	public static void main(String[] args) {
		DataTypeIfazeMethodReturnTypeUnboxing ifaze = new Main();
		Integer test = ifaze.method1();
	}
}
