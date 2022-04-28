package exceptionClazzMethodTryCatchToThrowChecked;

import java.io.File;

import testing_lib.exceptionClazzMethodTryCatchToThrowChecked.ExceptionClazzMethodTryCatchToThrowChecked;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodTryCatchToThrowChecked constr = new ExceptionClazzMethodTryCatchToThrowChecked();
		constr.method1(new File("test"));
	}
	
}
