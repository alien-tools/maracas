package genericsWildcardsClazzMethodParamUpperBoundsToLowerBounds;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamUpperBoundsToLowerBounds.GenericsWildcardsClazzMethodParamUpperBoundsToLowerBounds;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamUpperBoundsToLowerBounds constr = new GenericsWildcardsClazzMethodParamUpperBoundsToLowerBounds();
		ArrayList<Integer> param1 = new ArrayList<Integer>();
		constr.method1(param1);
		
		ArrayList<Number> param2 = new ArrayList<Number>();
		constr.method1(param2);
	}
	
}
