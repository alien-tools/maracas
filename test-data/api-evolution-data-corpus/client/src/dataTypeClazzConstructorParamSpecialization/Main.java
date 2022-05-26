package dataTypeClazzConstructorParamSpecialization;

import testing_lib.dataTypeClazzConstructorParamSpecialization.DataTypeClazzConstructorParamSpecialization;

public class Main {

	public static void main(String[] args) {

		Number param1 = 5;
		DataTypeClazzConstructorParamSpecialization constr = new DataTypeClazzConstructorParamSpecialization(param1);
		constr.toString();
	}

}
