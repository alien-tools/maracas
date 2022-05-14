package exceptionClazzMethodCatchBlockDelete;

import java.io.File;

import testing_lib.exceptionClazzMethodCatchBlockDelete.ExceptionClazzMethodCatchBlockDelete;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodCatchBlockDelete constr = new ExceptionClazzMethodCatchBlockDelete();
		constr.method1(new File("test"));
	}
	
}
