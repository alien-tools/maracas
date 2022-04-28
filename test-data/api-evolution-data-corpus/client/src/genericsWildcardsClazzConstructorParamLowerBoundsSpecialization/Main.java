package genericsWildcardsClazzConstructorParamLowerBoundsSpecialization;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamLowerBoundsSpecialization.GenericsWildcardsClazzConstructorParamLowerBoundsSpecializaion;

public class Main {

	public static void main(String[] args) {
		ArrayList<Number> param1 = new ArrayList<Number>(); 
		GenericsWildcardsClazzConstructorParamLowerBoundsSpecializaion constr =  new GenericsWildcardsClazzConstructorParamLowerBoundsSpecializaion(param1);
		constr.toString();
	}
	
}
