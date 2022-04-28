package exceptionClazzMethodThrowCheckedMutation;

import java.io.FileNotFoundException;

import testing_lib.exceptionClazzMethodThrowCheckedMutation.ExceptionClazzMethodThrowCheckedMutation;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodThrowCheckedMutation constr = new ExceptionClazzMethodThrowCheckedMutation();
		try {
			constr.method1();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
