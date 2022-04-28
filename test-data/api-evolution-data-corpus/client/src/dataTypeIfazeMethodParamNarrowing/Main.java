package dataTypeIfazeMethodParamNarrowing;

import testing_lib.dataTypeIfazeMethodParamNarrowing.DataTypeIfazeMethodParamNarrowing;

public class Main implements DataTypeIfazeMethodParamNarrowing {

	@Override
	public void method1(double param1) {
		
	}
	
	public static void main(String[] args) {
		DataTypeIfazeMethodParamNarrowing ifaze = new Main();
		ifaze.method1(5);
	}

}
