package dataTypeIfazeMethodReturnTypeBoxing;

import testing_lib.dataTypeIfazeMethodReturnTypeBoxing.DataTypeIfazeMethodReturnTypeBoxing;

public class Main implements DataTypeIfazeMethodReturnTypeBoxing{

	@Override
	public int method1() {
		int result = 5;
		return result;
	}
	
	public static void main(String[] args) {

		DataTypeIfazeMethodReturnTypeBoxing constr = new Main();
		int test = constr.method1();
		
	}

}
