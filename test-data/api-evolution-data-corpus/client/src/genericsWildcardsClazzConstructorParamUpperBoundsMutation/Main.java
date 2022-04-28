package genericsWildcardsClazzConstructorParamUpperBoundsMutation;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamUpperBoundsMutation.GenericsWildcardsClazzConstructorParamUpperBoundsMutation;

public class Main {

	public static void main(String[] args) {
		ArrayList<Integer> param1 = new ArrayList<Integer>(); 
		GenericsWildcardsClazzConstructorParamUpperBoundsMutation constr =  new GenericsWildcardsClazzConstructorParamUpperBoundsMutation(param1);
		constr.toString();
	}
	
}
