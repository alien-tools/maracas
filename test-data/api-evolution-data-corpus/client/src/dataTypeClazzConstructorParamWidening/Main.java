package dataTypeClazzConstructorParamWidening;

import testing_lib.dataTypeClazzConstructorParamWidening.DataTypeClazzConstructorParamWidening;

public class Main {

	public static void main(String[] args) {

		int param1 = 5;
		DataTypeClazzConstructorParamWidening constr = new DataTypeClazzConstructorParamWidening(param1);
		constr.toString();
	}

}
