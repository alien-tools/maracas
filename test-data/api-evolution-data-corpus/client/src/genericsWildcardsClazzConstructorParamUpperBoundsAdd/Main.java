package genericsWildcardsClazzConstructorParamUpperBoundsAdd;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamUpperBoundsAdd.GenericsWildcardsClazzConstructorParamUpperBoundsAdd;

public class Main {

	public static void main(String[] args) {
		ArrayList<Object> param1 = new ArrayList<Object>(); 
		GenericsWildcardsClazzConstructorParamUpperBoundsAdd constr =  new GenericsWildcardsClazzConstructorParamUpperBoundsAdd(param1);
		constr.toString();
	}
	
}
