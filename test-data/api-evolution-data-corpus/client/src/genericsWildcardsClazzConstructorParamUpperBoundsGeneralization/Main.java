package genericsWildcardsClazzConstructorParamUpperBoundsGeneralization;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamUpperBoundsGeneralization.GenericsWildcardsClazzConstructorParamUpperBoundsGeneralization;

public class Main {

	public static void main(String[] args) {
		ArrayList<Integer> param1 = new ArrayList<Integer	>(); 
		GenericsWildcardsClazzConstructorParamUpperBoundsGeneralization constr =  new GenericsWildcardsClazzConstructorParamUpperBoundsGeneralization(param1);
		constr.toString();
	}
	
}
