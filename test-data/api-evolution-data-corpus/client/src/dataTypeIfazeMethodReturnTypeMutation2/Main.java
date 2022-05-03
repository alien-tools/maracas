package dataTypeIfazeMethodReturnTypeMutation2;

import testing_lib.dataTypeIfazeMethodReturnTypeMutation.DataTypeIfazeMethodReturnTypeMutation;

public class Main implements DataTypeIfazeMethodReturnTypeMutation {

	@Override
	public void methodVoidToInteger() {

	}

	@Override
	public Integer methodIntegerToVoid() {

		return Integer.valueOf(4);
	}

	@Override
	public Integer methodIntegerToString() {

		return Integer.valueOf(5);
	}

	public static void main(String[] args) {
		DataTypeIfazeMethodReturnTypeMutation ifaze = new Main();
		int test = ifaze.methodIntegerToVoid();
	}
}
