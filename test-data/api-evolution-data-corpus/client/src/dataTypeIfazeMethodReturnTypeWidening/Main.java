package dataTypeIfazeMethodReturnTypeWidening;

import testing_lib.dataTypeIfazeMethodReturnTypeWidening.DataTypeIfazeMethodReturnTypeWidening;

public class Main implements DataTypeIfazeMethodReturnTypeWidening {
	
	@Override
	public int method1() {
		
		return 0;
	}
	
	public static void main(String[] args) {
		DataTypeIfazeMethodReturnTypeWidening ifaze = new Main();
		int test = ifaze.method1();
	}
}
