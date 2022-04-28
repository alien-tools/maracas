package dataTypeClazzConstructorParamNarrowing;

import testing_lib.dataTypeClazzConstructorParamNarrowing.DataTypeClazzConstructorParamNarrowing;

public class Main {

	public static void main(String[] args) {
		
		double param1 = 5;
		DataTypeClazzConstructorParamNarrowing constr = new DataTypeClazzConstructorParamNarrowing(param1);
		constr.toString();
	}

}
