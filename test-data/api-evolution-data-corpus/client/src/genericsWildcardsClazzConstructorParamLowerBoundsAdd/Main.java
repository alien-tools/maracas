package genericsWildcardsClazzConstructorParamLowerBoundsAdd;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamLowerBoundsAdd.GenericsWildcardsClazzConstructorParamLowerBoundsAdd;

public class Main {

	public static void main(String[] args) {
		ArrayList<String> param1 = new ArrayList<String>(); 
		GenericsWildcardsClazzConstructorParamLowerBoundsAdd constr =  new GenericsWildcardsClazzConstructorParamLowerBoundsAdd(param1);
		constr.toString();
	}
	
}
