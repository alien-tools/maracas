package genericsWildcardsClazzMethodParamDelete;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamDelete.GenericsWildcardsClazzMethodParamDelete;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamDelete constr = new GenericsWildcardsClazzMethodParamDelete();
		ArrayList<Integer> param1 = new ArrayList<Integer>();
		constr.method1(param1);
		
	}
	
}
