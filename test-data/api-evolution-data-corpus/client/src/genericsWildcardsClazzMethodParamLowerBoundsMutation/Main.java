package genericsWildcardsClazzMethodParamLowerBoundsMutation;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamLowerBoundsMutation.GenericsWildcardsClazzMethodParamLowerBoundsMutation;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamLowerBoundsMutation constr = new GenericsWildcardsClazzMethodParamLowerBoundsMutation();
		ArrayList<Integer> param1 = new ArrayList<Integer>();
		constr.method1(param1);
	}
	
}
