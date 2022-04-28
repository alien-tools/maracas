package genericsWildcardsClazzConstructorParamLowerBoundsDelete;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamLowerBoundsDelete.GenericsWildcardsClazzConstructorParamLowerBoundsDelete;

public class Main {

	public static void main(String[] args) {
		ArrayList<Number> param1 = new ArrayList<Number>(); 
		GenericsWildcardsClazzConstructorParamLowerBoundsDelete constr =  new GenericsWildcardsClazzConstructorParamLowerBoundsDelete(param1);
		constr.toString();
	}
	
}
