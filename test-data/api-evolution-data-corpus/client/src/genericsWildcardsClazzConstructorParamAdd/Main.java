package genericsWildcardsClazzConstructorParamAdd;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamAdd.GenericsWildcardsClazzConstructorParamAdd;

public class Main {

	public static void main(String[] args) {
		ArrayList<Integer> param1 = new ArrayList<Integer>(); 
		GenericsWildcardsClazzConstructorParamAdd constr =  new GenericsWildcardsClazzConstructorParamAdd(param1);
		constr.toString();
	}
	
}
