package exceptionClazzMethodFinallyBlockDelete;

import java.io.File;

import testing_lib.exceptionClazzMethodFinallyBlockDelete.ExceptionClazzMethodFinallyBlockDelete;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodFinallyBlockDelete constr = new ExceptionClazzMethodFinallyBlockDelete();
		constr.method1(new File("test"));
	}
	
}
