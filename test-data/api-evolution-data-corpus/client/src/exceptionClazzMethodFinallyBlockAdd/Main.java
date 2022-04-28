package exceptionClazzMethodFinallyBlockAdd;

import java.io.File;

import testing_lib.exceptionClazzMethodFinallyBlockAdd.ExceptionClazzMethodFinallyBlockAdd;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodFinallyBlockAdd constr = new ExceptionClazzMethodFinallyBlockAdd();
		constr.method1(new File("test"));
	}
	
}
