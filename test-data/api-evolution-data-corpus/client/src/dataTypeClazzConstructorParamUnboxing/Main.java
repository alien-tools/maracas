package dataTypeClazzConstructorParamUnboxing;

import testing_lib.dataTypeClazzConstructorParamUnboxing.DataTypeClazzConstructorParamUnboxing;

public class Main {

	public static void main(String[] args) {

		Integer param1 = 5;
		DataTypeClazzConstructorParamUnboxing constr = new DataTypeClazzConstructorParamUnboxing(param1);
		constr.toString();
	}

}
