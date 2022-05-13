package genericsWildcardsClazzConstructorParamUpperBoundsToLowerBounds;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamUpperBoundsToLowerBounds.GenericsWildcardsClazzConstructorParamUpperBoundsToLowerBounds;

public class Main {

	public static void main(String[] args) {
		ArrayList<Integer> param1 = new ArrayList<Integer>(); 
		GenericsWildcardsClazzConstructorParamUpperBoundsToLowerBounds constr =  new GenericsWildcardsClazzConstructorParamUpperBoundsToLowerBounds(param1);
		constr.toString();
	}
	
}
