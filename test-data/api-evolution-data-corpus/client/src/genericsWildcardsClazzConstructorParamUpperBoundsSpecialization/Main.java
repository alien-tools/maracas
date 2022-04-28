package genericsWildcardsClazzConstructorParamUpperBoundsSpecialization;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzConstructorParamUpperBoundsSpecialization.GenericsWildcardsClazzConstructorParamUpperBoundsSpecialization;

public class Main {

	public static void main(String[] args) {
		ArrayList<Number> param1 = new ArrayList<Number>(); 
		GenericsWildcardsClazzConstructorParamUpperBoundsSpecialization constr =  new GenericsWildcardsClazzConstructorParamUpperBoundsSpecialization(param1);
		constr.toString();
	}
	
}
