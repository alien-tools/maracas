package dataTypeIfazeMethodReturnTypeGeneralization;

import testing_lib.dataTypeIfazeMethodReturnTypeGeneralization.DataTypeIfazeMethodReturnTypeGeneralization;

public class Main implements DataTypeIfazeMethodReturnTypeGeneralization {

	@Override
	public Integer method1() {
		return new Integer(5);
	}
	
	public static void main(String[] args) {

		DataTypeIfazeMethodReturnTypeGeneralization constr = new Main();
		Integer test = constr.method1();
		
	}

}
