package dataTypeIfazeMethodReturnTypeMutation3;

import testing_lib.dataTypeIfazeMethodReturnTypeMutation.DataTypeIfazeMethodReturnTypeMutation;

public class Main implements DataTypeIfazeMethodReturnTypeMutation {
	
	@Override
	public void methodVoidToInteger() {
		
	}

	@Override
	public Integer methodIntegerToVoid() {
		
		return new Integer(4);
	}

	@Override
	public Integer methodIntegerToString() {
		
		return new Integer(5);
	}
	
	public static void main(String[] args) {
		DataTypeIfazeMethodReturnTypeMutation ifaze = new Main();
		ifaze.methodVoidToInteger();
	}
}
