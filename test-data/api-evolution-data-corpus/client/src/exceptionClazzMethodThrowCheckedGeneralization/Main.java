package exceptionClazzMethodThrowCheckedGeneralization;

import java.io.FileNotFoundException;

import testing_lib.exceptionClazzMethodThrowCheckedGeneralization.ExceptionClazzMethodThrowCheckedGeneralization;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodThrowCheckedGeneralization constr = new ExceptionClazzMethodThrowCheckedGeneralization();
		try {
			constr.method1();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
