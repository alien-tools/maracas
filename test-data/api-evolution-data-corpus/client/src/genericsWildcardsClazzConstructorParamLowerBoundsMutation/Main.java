package genericsWildcardsClazzConstructorParamLowerBoundsMutation;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamLowerBoundsMutation.GenericsWildcardsClazzConstructorParamLowerBoundsMutation;

public class Main {

	public static void main(String[] args) {
		ArrayList<Integer> param1 = new ArrayList<Integer>(); 
		GenericsWildcardsClazzConstructorParamLowerBoundsMutation constr =  new GenericsWildcardsClazzConstructorParamLowerBoundsMutation(param1);
		constr.toString();
	}
	
}
