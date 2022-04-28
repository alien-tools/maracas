package genericsWildcardsClazzConstructorParamUpperBoundsDelete;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamUpperBoundsDelete.GenericsWildcardsClazzConstructorParamUpperBoundsDelete;

public class Main {

	public static void main(String[] args) {
		ArrayList<Number> param1 = new ArrayList<Number>(); 
		GenericsWildcardsClazzConstructorParamUpperBoundsDelete constr =  new GenericsWildcardsClazzConstructorParamUpperBoundsDelete(param1);
		constr.toString();
	}
	
}
