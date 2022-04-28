package genericsWildcardsClazzMethodParamLowerBoundsGeneralization;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamLowerBoundsGeneralization.GenericsWildcardsClazzMethodParamLowerBoundsGeneralization;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamLowerBoundsGeneralization constr = new GenericsWildcardsClazzMethodParamLowerBoundsGeneralization();
		ArrayList<Integer> param1 = new ArrayList<Integer>();
		constr.method1(param1);
	}
	
}
