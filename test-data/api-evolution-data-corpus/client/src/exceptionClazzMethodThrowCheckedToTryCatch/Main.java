package exceptionClazzMethodThrowCheckedToTryCatch;

import java.io.File;
import java.io.IOException;

import testing_lib.exceptionClazzMethodThrowCheckedToTryCatch.ExceptionClazzMethodThrowCheckedToTryCatch;

public class Main {

	public static void main(String[] args) {
		ExceptionClazzMethodThrowCheckedToTryCatch constr = new ExceptionClazzMethodThrowCheckedToTryCatch();
		try {
			constr.method1(new File("test"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
