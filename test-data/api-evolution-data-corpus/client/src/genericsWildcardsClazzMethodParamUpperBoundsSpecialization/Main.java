package genericsWildcardsClazzMethodParamUpperBoundsSpecialization;

import java.util.ArrayList;

import testing_lib.genericsWildcardsClazzMethodParamUpperBoundsSpecialization.GenericsWildcardsClazzMethodParamUpperBoundsSpecialization;

public class Main {

	public static void main(String[] args) {
		GenericsWildcardsClazzMethodParamUpperBoundsSpecialization constr = new GenericsWildcardsClazzMethodParamUpperBoundsSpecialization();
		
		ArrayList<Number> param1 = new ArrayList<Number>();
		constr.method1(param1);
		
	}
	
}
