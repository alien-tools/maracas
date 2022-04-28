package exceptionClazzMethodThrowCheckedDelete;

import java.io.IOException;

import testing_lib.exceptionClazzMethodThrowCheckedDelete.ExceptionClazzMethodThrowCheckedDelete;

public class Main {
	public static void main(String[] args) {
		ExceptionClazzMethodThrowCheckedDelete constr = new ExceptionClazzMethodThrowCheckedDelete();
		try {
			constr.method1();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
