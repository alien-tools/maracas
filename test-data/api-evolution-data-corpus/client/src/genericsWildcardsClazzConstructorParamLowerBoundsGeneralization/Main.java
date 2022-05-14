package genericsWildcardsClazzConstructorParamLowerBoundsGeneralization;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamLowerBoundsGeneralization.GenericsWildcardsClazzConstructorParamLowerBoundsGeneralization;

public class Main {

	public static void main(String[] args) {
		ArrayList<Integer> param1 = new ArrayList<Integer>(); 
		GenericsWildcardsClazzConstructorParamLowerBoundsGeneralization constr =  new GenericsWildcardsClazzConstructorParamLowerBoundsGeneralization(param1);
		constr.toString();
	}
	
}
