package exceptionClazzMethodCatchBlockAdd;

import java.io.File;

import testing_lib.exceptionClazzMethodCatchBlockAdd.ExceptionClazzMethodCatchBlockAdd;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodCatchBlockAdd constr = new ExceptionClazzMethodCatchBlockAdd();
		constr.method1(new File("test"));
	}
	
}
