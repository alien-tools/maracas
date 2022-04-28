package exceptionClazzMethodThrowCheckedSpecialization;

import java.io.IOException;

import testing_lib.exceptionClazzMethodThrowCheckedSpecialization.ExceptionClazzMethodThrowCheckedSpecialization;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodThrowCheckedSpecialization constr = new ExceptionClazzMethodThrowCheckedSpecialization();
		try {
			constr.method1();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
