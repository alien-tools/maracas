package testing_lib.exceptionClazzMethodTryCatchToThrowChecked;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ExceptionClazzMethodTryCatchToThrowChecked {

	public void method1(File file) throws IOException {
		FileReader fr = new FileReader(file); 
		fr.close();
	}
	
}
