package genericsWildcardsClazzConstructorParamLowerBoundsToUpperBounds;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamLowerBoundsToUpperBounds.GenericsWildcardsClazzConstructorParamLowerBoundsToUpperBounds;

public class Main {

	public static void main(String[] args) {
		ArrayList<Object> param1 = new ArrayList<Object>(); 
		GenericsWildcardsClazzConstructorParamLowerBoundsToUpperBounds constr =  new GenericsWildcardsClazzConstructorParamLowerBoundsToUpperBounds(param1);
		constr.toString();
	}
	
}
