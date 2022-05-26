package genericsWildcardsClazzMethodParamLowerBoundsToUpperBounds;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamLowerBoundsToUpperBounds.GenericsWildcardsClazzMethodParamLowerBoundsToUpperBounds;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamLowerBoundsToUpperBounds constr = new GenericsWildcardsClazzMethodParamLowerBoundsToUpperBounds();
		ArrayList<Object> param1 = new ArrayList<Object>();
		constr.method1(param1);
		
		ArrayList<Number> param2 = new ArrayList<Number>();
		constr.method1(param2);
	}
	
}
