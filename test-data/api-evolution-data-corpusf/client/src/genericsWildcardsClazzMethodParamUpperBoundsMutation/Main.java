package genericsWildcardsClazzMethodParamUpperBoundsMutation;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamUpperBoundsMutation.GenericsWildcardsClazzMethodParamUpperBoundsMutation;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamUpperBoundsMutation constr = new GenericsWildcardsClazzMethodParamUpperBoundsMutation();
		ArrayList<Integer> param1 = new ArrayList<Integer>();
		constr.method1(param1);
	}
	
}
